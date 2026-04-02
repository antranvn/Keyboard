package com.securekey.sdk

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.securekey.sdk.core.InputProcessor
import com.securekey.sdk.core.KeyboardLayout
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.core.LayoutEngine
import com.securekey.sdk.security.AntiKeylogProtection
import com.securekey.sdk.security.ClipboardGuard
import com.securekey.sdk.security.IntegrityGuard
import com.securekey.sdk.security.IntegrityReport
import com.securekey.sdk.security.SecureKeyDispatcher
import com.securekey.sdk.security.SecureMemoryManager
import com.securekey.sdk.ui.FocusManager
import com.securekey.sdk.ui.SecureEditText
import com.securekey.sdk.ui.SecureKeyboardView
import com.securekey.sdk.ui.SystemKeyboardSuppressor

/**
 * Main entry point for the SecureKey SDK.
 *
 * Usage:
 * ```kotlin
 * // In Application.onCreate()
 * val config = SecureKeyConfig.Builder()
 *     .theme(ThemePresets.system(this))
 *     .build()
 * SecureKey.init(this, config)
 *
 * // In any Activity.onCreate()
 * SecureKey.getInstance().bind(this)
 * SecureKey.getInstance().attachTo(editText, KeyboardMode.QWERTY_FULL)
 * ```
 */
class SecureKey private constructor(
    private val context: Context,
    private val config: SecureKeyConfig
) {
    private val dispatcher = SecureKeyDispatcher()
    private val memoryManager = SecureMemoryManager()
    private val integrityGuard = IntegrityGuard(context)
    private val antiKeylog = AntiKeylogProtection(context)
    private val clipboardGuard = ClipboardGuard(context)
    private val focusManager = FocusManager()
    private val suppressor = SystemKeyboardSuppressor(context)
    private val inputProcessor = InputProcessor(dispatcher)

    private var keyboardView: SecureKeyboardView? = null
    private var currentMode: KeyboardMode? = null
    private var currentBaseLayout: KeyboardLayout? = null
    private var threatListener: OnSecurityThreatListener? = config.onSecurityThreat
    private var keyboardStateListener: OnKeyboardStateListener? = null
    private var activityRef: java.lang.ref.WeakReference<Activity>? = null
    private var isKeyboardVisible = false

    init {
        dispatcher.initSession()
        setupFocusManager()
    }

    /** Bind to an Activity for keyboard view attachment and FLAG_SECURE. Call from onCreate. */
    fun bind(activity: Activity) {
        // If switching activities, clean up old keyboard view
        val oldActivity = activityRef?.get()
        if (oldActivity != null && oldActivity !== activity) {
            cleanupKeyboardView()
        }
        activityRef = java.lang.ref.WeakReference(activity)
        if (!config.allowScreenshot) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /** Attach the secure keyboard to an EditText */
    fun attachTo(editText: EditText, mode: KeyboardMode) {
        if (editText is SecureEditText) {
            clipboardGuard.clearClipboard()
        }
        suppressor.suppress(editText)
        focusManager.register(editText, mode)

        if (editText.hasFocus()) {
            showForMode(mode, editText)
        }
    }

    /** Attach multiple EditTexts for OTP entry */
    fun attachOtpFields(vararg editTexts: EditText, otpLength: Int = 6) {
        editTexts.take(otpLength).forEachIndexed { index, editText ->
            attachTo(editText, KeyboardMode.NUMERIC_OTP)
        }
    }

    /** Attach an EditText for amount entry */
    fun attachAmountField(
        editText: EditText,
        currency: Currency = config.currency,
        suggestions: List<Double> = config.amountSuggestions,
        maxAmount: Double = config.maxAmount,
        decimalPlaces: Int = config.decimalPlaces
    ) {
        attachTo(editText, KeyboardMode.AMOUNT_PAD)
    }

    /** Show the keyboard */
    fun show() {
        val mode = currentMode ?: return
        keyboardView?.show()
    }

    /** Dismiss the keyboard */
    fun dismiss() {
        keyboardView?.dismiss {
            memoryManager.onKeyboardDismiss()
            isKeyboardVisible = false
            keyboardStateListener?.onKeyboardHidden()
        }
    }

    /** Get the current keyboard height in pixels (0 if hidden) */
    fun getKeyboardHeight(): Int {
        if (!isKeyboardVisible) return 0
        return keyboardView?.height ?: 0
    }

    /** Whether the keyboard is currently visible */
    fun isKeyboardVisible(): Boolean = isKeyboardVisible

    /** Set keyboard visibility state listener */
    fun setKeyboardStateListener(listener: OnKeyboardStateListener) {
        keyboardStateListener = listener
    }

    /** Get the current security report */
    fun getSecurityReport(): IntegrityReport {
        return integrityGuard.generateReport()
    }

    /** Set threat detection listener */
    fun setThreatListener(listener: OnSecurityThreatListener) {
        threatListener = listener
    }

    /** Call from Activity.onResume() */
    fun onResume() {
        checkThreats()
    }

    /** Call from Activity.onPause() */
    fun onPause() {
        memoryManager.onStop()
    }

    /**
     * Clean up activity-specific resources. Call from Activity.onDestroy().
     * The SDK singleton remains alive for reuse in other activities.
     * Call [destroy] only if you want to fully tear down the SDK.
     */
    fun onDestroy() {
        cleanupKeyboardView()
        focusManager.destroy()
        suppressor.destroy()
        // Re-setup focus manager for the next activity
        setupFocusManager()
    }

    /** Fully destroy the SDK instance and release all resources. */
    fun destroy() {
        cleanupKeyboardView()
        dispatcher.destroySession()
        memoryManager.wipeAll()
        clipboardGuard.stopMonitoring()
        focusManager.destroy()
        suppressor.destroy()
        inputProcessor.destroy()
        instance = null
    }

    private fun cleanupKeyboardView() {
        keyboardView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        keyboardView = null
        currentMode = null
        currentBaseLayout = null
        isKeyboardVisible = false
    }

    private fun showForMode(mode: KeyboardMode, targetView: EditText) {
        val alreadyVisible = keyboardView?.visibility == View.VISIBLE && currentMode == mode

        currentMode = mode
        val layout = LayoutEngine.generateLayout(
            mode = mode,
            shuffleNumericKeys = config.shuffleNumericKeys,
            doneLabel = config.doneButtonLabel,
            currencySymbol = config.currency.symbol
        )
        currentBaseLayout = layout

        ensureKeyboardView(targetView)
        val view = keyboardView ?: return

        if (!alreadyVisible) {
            config.theme.applyTo(view.renderer)
            view.touchHandler.hapticEnabled = config.hapticFeedbackEnabled
            // Reset shift state when switching to a new mode
            inputProcessor.getShiftState().reset()
            view.setLayout(layout)
        }

        // Always rebind input to the current target field
        inputProcessor.setOnCharacterInput { char ->
            targetView.append(char.toString())
            // After typing a character, update labels if shift was auto-reset
            refreshShiftLabels()
        }
        inputProcessor.setOnActionKey { action ->
            when (action) {
                "BACKSPACE" -> {
                    val text = targetView.text
                    if (text != null && text.isNotEmpty()) {
                        text.delete(text.length - 1, text.length)
                    }
                }
                "DONE" -> dismiss()
                "SHIFT" -> refreshShiftLabels()
                "SYMBOLS" -> switchToSymbols(0)
                "SYMBOLS_2" -> switchToSymbols(1)
                "LETTERS" -> switchToLetters()
            }
        }
        view.onKeyPressed = { key ->
            inputProcessor.processKey(key)
        }

        // Only animate show if not already visible
        if (!alreadyVisible) {
            view.show()
            isKeyboardVisible = true
            keyboardStateListener?.onKeyboardShown(view.height)
            checkThreats()
        }
    }

    /** Update keyboard labels to reflect current shift state */
    private fun refreshShiftLabels() {
        val view = keyboardView ?: return
        val baseLayout = currentBaseLayout ?: return
        val shiftActive = inputProcessor.getShiftState().isActive
        val shifted = baseLayout.withShiftApplied(shiftActive)
        view.setLayout(shifted)
    }

    private fun switchToSymbols(layer: Int) {
        val layout = LayoutEngine.generateSymbolLayout(layer, config.doneButtonLabel)
        currentBaseLayout = layout
        inputProcessor.getShiftState().reset()
        keyboardView?.setLayout(layout)
    }

    private fun switchToLetters() {
        val layout = LayoutEngine.generateLayout(
            mode = KeyboardMode.QWERTY_FULL,
            doneLabel = config.doneButtonLabel
        )
        currentBaseLayout = layout
        inputProcessor.getShiftState().reset()
        keyboardView?.setLayout(layout)
    }

    /**
     * Find the best root ViewGroup for attaching the keyboard.
     * Supports dialogs, bottom sheets, and popups by walking up the view hierarchy.
     */
    private fun findRootViewFor(targetView: View): ViewGroup? {
        // Walk up to find the top-most FrameLayout (dialog or activity root)
        var current: View? = targetView
        var lastFrameLayout: ViewGroup? = null
        while (current != null) {
            if (current is FrameLayout && current.id == android.R.id.content) {
                return current
            }
            if (current is FrameLayout) {
                lastFrameLayout = current
            }
            current = current.parent as? View
        }
        // If in a dialog/popup, use the topmost FrameLayout we found
        if (lastFrameLayout != null) return lastFrameLayout
        // Fallback to activity root
        val activity = activityRef?.get() ?: return null
        return activity.findViewById(android.R.id.content)
    }

    private fun ensureKeyboardView(targetView: View? = null) {
        if (keyboardView != null) return

        val rootView = if (targetView != null) {
            findRootViewFor(targetView)
        } else {
            val activity = activityRef?.get() ?: (context as? Activity) ?: return
            activity.findViewById<ViewGroup>(android.R.id.content)
        } ?: return

        val viewContext = rootView.context
        val view = SecureKeyboardView(viewContext)
        val density = viewContext.resources.displayMetrics.density
        view.renderer.setDensity(density)

        // Get navigation bar height for bottom inset
        val navBarHeight = getNavigationBarHeight(rootView)
        val totalHeight = (KEYBOARD_HEIGHT_DP * density).toInt() + navBarHeight
        view.bottomInsetPx = navBarHeight.toFloat()

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            totalHeight
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }

        rootView.addView(view, params)
        keyboardView = view

        // Also listen for inset changes (e.g. rotation)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.bottomInsetPx = navInset.bottom.toFloat()
            val lp = v.layoutParams as FrameLayout.LayoutParams
            lp.height = (KEYBOARD_HEIGHT_DP * density).toInt() + navInset.bottom
            v.layoutParams = lp
            if (isKeyboardVisible) {
                keyboardStateListener?.onKeyboardShown(lp.height)
            }
            insets
        }
    }

    private fun getNavigationBarHeight(rootView: View): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(rootView)
        return windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
    }

    private fun setupFocusManager() {
        focusManager.setOnShowKeyboard { view, mode ->
            if (view is EditText) {
                showForMode(mode, view)
            }
        }
        focusManager.setOnDismissKeyboard {
            dismiss()
        }
    }

    private fun checkThreats() {
        val report = integrityGuard.generateReport()
        val listener = threatListener ?: return

        report.detectedThreats.forEach { threat ->
            val details = when (threat) {
                com.securekey.sdk.security.ThreatType.ACCESSIBILITY_KEYLOGGER ->
                    report.activeKeyloggers.joinToString()
                else -> threat.name
            }
            listener.onThreatDetected(threat, details)
        }
    }

    companion object {
        /** Keyboard height in dp — matches Gboard compact layout */
        internal const val KEYBOARD_HEIGHT_DP = 230

        @Volatile
        private var instance: SecureKey? = null

        /** Initialize the SDK. Call once from Application.onCreate(). */
        fun init(context: Context, config: SecureKeyConfig = SecureKeyConfig.Builder().build()) {
            instance = SecureKey(context.applicationContext, config)
        }

        /** Get the singleton instance */
        fun getInstance(): SecureKey {
            return requireNotNull(instance) { "SecureKey.init() must be called first" }
        }
    }
}
