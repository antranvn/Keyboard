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
 * val config = SecureKeyConfig.Builder()
 *     .theme(ThemePresets.system(this))
 *     .shuffleNumericKeys(true)
 *     .build()
 *
 * SecureKey.init(this, config)
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
    private var activityRef: java.lang.ref.WeakReference<Activity>? = null

    init {
        dispatcher.initSession()
        setupFocusManager()
    }

    /** Bind to an Activity for keyboard view attachment and FLAG_SECURE. Call from onCreate. */
    fun bind(activity: Activity) {
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
        }
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

    /** Call from Activity.onDestroy() */
    fun onDestroy() {
        keyboardView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        dispatcher.destroySession()
        memoryManager.wipeAll()
        clipboardGuard.stopMonitoring()
        focusManager.destroy()
        suppressor.destroy()
        inputProcessor.destroy()
        keyboardView = null
        instance = null
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

        ensureKeyboardView()
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
            // Shift key is handled via onActionKey callback above
        }

        // Only animate show if not already visible
        if (!alreadyVisible) {
            view.show()
            checkThreats()
        }
    }

    /** Update keyboard labels to reflect current shift state */
    private fun refreshShiftLabels() {
        val view = keyboardView ?: return
        val baseLayout = currentBaseLayout ?: return
        val shiftActive = inputProcessor.getShiftState().isActive
        val shifted = baseLayout.withShiftApplied(shiftActive)
        // Re-calculate bounds with the shifted labels
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

    private fun ensureKeyboardView() {
        if (keyboardView != null) return

        val activity = activityRef?.get() ?: (context as? Activity) ?: return
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val view = SecureKeyboardView(activity)
        val density = activity.resources.displayMetrics.density
        view.renderer.setDensity(density)

        // Get navigation bar height for bottom inset
        val navBarHeight = getNavigationBarHeight(activity)
        val totalHeight = (300 * density).toInt() + navBarHeight
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
            lp.height = (300 * density).toInt() + navInset.bottom
            v.layoutParams = lp
            insets
        }
    }

    private fun getNavigationBarHeight(activity: Activity): Int {
        val windowInsets = ViewCompat.getRootWindowInsets(
            activity.window.decorView
        )
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
        @Volatile
        private var instance: SecureKey? = null

        /** Initialize the SDK. Call once from Application or Activity. */
        fun init(context: Context, config: SecureKeyConfig = SecureKeyConfig.Builder().build()) {
            instance = SecureKey(context.applicationContext, config)
        }

        /** Get the singleton instance */
        fun getInstance(): SecureKey {
            return requireNotNull(instance) { "SecureKey.init() must be called first" }
        }
    }
}
