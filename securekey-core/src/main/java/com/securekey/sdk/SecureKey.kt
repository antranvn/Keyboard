package com.securekey.sdk

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
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
import com.securekey.sdk.ui.KeyboardActionStrip
import com.securekey.sdk.ui.SecureEditText
import com.securekey.sdk.ui.SecureKeyboardView
import com.securekey.sdk.ui.SystemKeyboardSuppressor

/**
 * Main entry point for the SecureKey SDK.
 *
 * The keyboard behaves like the native system keyboard: it pushes content up
 * by applying bottom padding to the content root when it shows, and removes
 * it when it hides. Integrating apps do not need to handle layout adjustments.
 *
 * Usage:
 * ```kotlin
 * // In Application.onCreate()
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
    private var keyboardHost: LinearLayout? = null
    private var actionStrip: KeyboardActionStrip? = null
    private var actionLabel: String? = null
    private var actionIconRes: Int? = null
    private var actionOnClick: (() -> Unit)? = null
    private var actionAllowedModes: Set<KeyboardMode> = setOf(KeyboardMode.QWERTY_FULL)
    private var currentMode: KeyboardMode? = null
    private var currentBaseLayout: KeyboardLayout? = null
    private var threatListener: OnSecurityThreatListener? = config.onSecurityThreat
    private var keyboardStateListener: OnKeyboardStateListener? = null
    private var activityRef: java.lang.ref.WeakReference<Activity>? = null
    private var isKeyboardVisible = false

    // Track the content root and its original padding so we can push content up
    private var contentRoot: ViewGroup? = null
    private var originalContentBottomPadding: Int = 0
    /** Pre-calculated keyboard height in px (available immediately, no layout pass needed) */
    private var keyboardHeightPx: Int = 0

    init {
        dispatcher.initSession()
        setupFocusManager()
    }

    /** Bind to an Activity for keyboard view attachment and FLAG_SECURE. Call from onCreate. */
    fun bind(activity: Activity) {
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
        editTexts.take(otpLength).forEachIndexed { _, editText ->
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
        currentMode ?: return
        keyboardView?.show()
    }

    /** Dismiss the keyboard */
    fun dismiss() {
        if (!isKeyboardVisible) return
        val root = contentRoot
        val startPadding = root?.paddingBottom ?: 0
        val endPadding = originalContentBottomPadding

        // Animate content padding down in sync with keyboard slide-out
        if (root != null && startPadding != endPadding) {
            val animator = ValueAnimator.ofInt(startPadding, endPadding).apply {
                duration = DISMISS_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    root.setPadding(
                        root.paddingLeft,
                        root.paddingTop,
                        root.paddingRight,
                        anim.animatedValue as Int
                    )
                }
            }
            animator.start()
        }

        keyboardView?.dismiss {
            memoryManager.onKeyboardDismiss()
            // Ensure final padding is correct after animation
            root?.setPadding(
                root.paddingLeft,
                root.paddingTop,
                root.paddingRight,
                originalContentBottomPadding
            )
            keyboardHost?.visibility = View.GONE
            isKeyboardVisible = false
            keyboardStateListener?.onKeyboardHidden()
        }
    }

    /**
     * Get the current keyboard height in pixels.
     * Returns the pre-calculated height immediately — no layout pass needed.
     * Returns 0 if the keyboard is hidden.
     */
    fun getKeyboardHeight(): Int {
        if (!isKeyboardVisible) return 0
        return totalKeyboardHeightPx()
    }

    /** Whether the keyboard is currently visible */
    fun isKeyboardVisible(): Boolean = isKeyboardVisible

    /** Set keyboard visibility state listener */
    fun setKeyboardStateListener(listener: OnKeyboardStateListener) {
        keyboardStateListener = listener
    }

    /**
     * Show a labelled action in a strip above the keyboard. The SDK renders the
     * strip; the host app owns the click handler. Typical use: "Sign in with
     * saved password" wired to Credential Manager.
     *
     * The strip appears only for keyboard modes in [allowedModes] — by default
     * only [KeyboardMode.QWERTY_FULL], so it never shows on PIN/OTP/amount pads.
     *
     * @param label text shown in the strip
     * @param iconRes optional drawable shown before the label (null = text only)
     * @param allowedModes which keyboard modes should display this action
     * @param onClick invoked on tap; SDK does not debounce
     */
    fun setKeyboardAction(
        label: String,
        iconRes: Int? = null,
        allowedModes: Set<KeyboardMode> = setOf(KeyboardMode.QWERTY_FULL),
        onClick: () -> Unit
    ) {
        actionLabel = label
        actionIconRes = iconRes
        actionAllowedModes = allowedModes
        actionOnClick = onClick
        refreshActionStrip()
    }

    /** Remove any previously set keyboard action. */
    fun clearKeyboardAction() {
        actionLabel = null
        actionIconRes = null
        actionOnClick = null
        refreshActionStrip()
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
     */
    fun onDestroy() {
        paddingAnimator?.cancel()
        paddingAnimator = null
        restoreContentPadding()
        cleanupKeyboardView()
        focusManager.destroy()
        suppressor.destroy()
        setupFocusManager()
    }

    /** Fully destroy the SDK instance and release all resources. */
    fun destroy() {
        restoreContentPadding()
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
        keyboardHost?.let { host ->
            (host.parent as? ViewGroup)?.removeView(host)
        }
        keyboardHost = null
        keyboardView = null
        actionStrip = null
        currentMode = null
        currentBaseLayout = null
        isKeyboardVisible = false
        contentRoot = null
    }

    private fun actionStripHeightPx(): Int {
        val density = context.resources.displayMetrics.density
        return (KeyboardActionStrip.HEIGHT_DP * density).toInt()
    }

    private fun isActionVisibleForCurrentMode(): Boolean {
        val label = actionLabel ?: return false
        val mode = currentMode ?: return false
        if (label.isBlank()) return false
        return mode in actionAllowedModes
    }

    private fun refreshActionStrip() {
        val strip = actionStrip ?: return
        val host = keyboardHost ?: return
        val shouldShow = isActionVisibleForCurrentMode()
        if (shouldShow) {
            strip.bind(actionLabel!!, actionIconRes) { actionOnClick?.invoke() }
            strip.visibility = View.VISIBLE
        } else {
            strip.visibility = View.GONE
        }
        updateHostHeight(host)
    }

    private fun totalKeyboardHeightPx(): Int {
        val strip = if (isActionVisibleForCurrentMode()) actionStripHeightPx() else 0
        return keyboardHeightPx + strip
    }

    private fun updateHostHeight(host: ViewGroup) {
        val newHeight = totalKeyboardHeightPx()
        val lp = host.layoutParams as? FrameLayout.LayoutParams ?: return
        if (lp.height != newHeight) {
            lp.height = newHeight
            host.layoutParams = lp
        }
        if (isKeyboardVisible) {
            val root = contentRoot
            if (root != null) {
                root.setPadding(
                    root.paddingLeft,
                    root.paddingTop,
                    root.paddingRight,
                    originalContentBottomPadding + newHeight
                )
            }
            keyboardStateListener?.onKeyboardShown(newHeight)
        }
    }

    private fun showForMode(mode: KeyboardMode, targetView: EditText) {
        // If focus arrived from a non-secure field, the system IME is still up.
        // Dismiss it before adding our own bottom padding so adjustResize doesn't
        // compound with applyContentPadding and over-push the layout.
        suppressor.hideSystemKeyboard(targetView)

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
            inputProcessor.getShiftState().reset()
            view.renderer.shiftActive = false
            view.renderer.capsLockOn = false
            view.setLayout(layout)
        }

        // Always rebind input to the current target field
        inputProcessor.setOnCharacterInput { char ->
            targetView.append(char.toString())
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

        // Swipe typing: enabled for all QWERTY_FULL fields.
        view.touchHandler.swipeEnabled = mode == KeyboardMode.QWERTY_FULL
        view.touchHandler.onSwipeCommitted = { keys ->
            keys.forEach { inputProcessor.processKey(it) }
        }

        refreshActionStrip()

        if (!alreadyVisible) {
            keyboardHost?.visibility = View.VISIBLE
            view.show()
            isKeyboardVisible = true
            animateContentPadding()
            keyboardStateListener?.onKeyboardShown(totalKeyboardHeightPx())
            // Scroll the focused field into view
            scrollFieldIntoView(targetView)
            checkThreats()
        }
    }

    /**
     * Push content up by adding bottom padding to the content root.
     * This mimics the native keyboard adjustResize behavior.
     */
    private fun applyContentPadding() {
        val root = contentRoot ?: return
        originalContentBottomPadding = root.paddingBottom
        root.setPadding(
            root.paddingLeft,
            root.paddingTop,
            root.paddingRight,
            originalContentBottomPadding + totalKeyboardHeightPx()
        )
    }

    private var paddingAnimator: ValueAnimator? = null

    private fun animateContentPadding() {
        val root = contentRoot ?: return
        originalContentBottomPadding = root.paddingBottom
        val target = originalContentBottomPadding + totalKeyboardHeightPx()

        paddingAnimator?.cancel()
        paddingAnimator = ValueAnimator.ofInt(root.paddingBottom, target).apply {
            duration = SHOW_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                root.setPadding(
                    root.paddingLeft,
                    root.paddingTop,
                    root.paddingRight,
                    anim.animatedValue as Int
                )
            }
            start()
        }
    }
    /** Restore original content padding when keyboard hides */
    private fun restoreContentPadding() {
        val root = contentRoot ?: return
        root.setPadding(
            root.paddingLeft,
            root.paddingTop,
            root.paddingRight,
            originalContentBottomPadding
        )
    }

    /**
     * Scroll the focused EditText into visible area above the keyboard,
     * similar to how the system keyboard scrolls focused views.
     */
    private fun scrollFieldIntoView(targetView: EditText) {
        targetView.post {
            // Find a scrollable parent and request scroll
            var parent = targetView.parent
            while (parent != null) {
                if (parent is ScrollView || parent is androidx.core.widget.NestedScrollView) {
                    val scrollView = parent as ViewGroup
                    val location = IntArray(2)
                    targetView.getLocationInWindow(location)
                    val scrollViewLocation = IntArray(2)
                    scrollView.getLocationInWindow(scrollViewLocation)

                    val fieldBottom = location[1] + targetView.height
                    val visibleBottom = scrollViewLocation[1] + scrollView.height - keyboardHeightPx

                    if (fieldBottom > visibleBottom) {
                        val scrollAmount = fieldBottom - visibleBottom + (16 * context.resources.displayMetrics.density).toInt()
                        (parent as? ScrollView)?.smoothScrollBy(0, scrollAmount)
                        (parent as? androidx.core.widget.NestedScrollView)?.smoothScrollBy(0, scrollAmount)
                    }
                    return@post
                }
                parent = parent.parent
            }
            // No ScrollView parent — use requestRectangleOnScreen as fallback
            val rect = android.graphics.Rect()
            targetView.getGlobalVisibleRect(rect)
            targetView.requestRectangleOnScreen(rect, false)
        }
    }

    private fun refreshShiftLabels() {
        val view = keyboardView ?: return
        val baseLayout = currentBaseLayout ?: return
        val shiftState = inputProcessor.getShiftState()
        view.renderer.shiftActive = shiftState.isActive
        view.renderer.capsLockOn = shiftState.isCapsLock
        val shifted = baseLayout.withShiftApplied(shiftState.isActive)
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
     * Supports activities, dialogs, bottom sheets, and popups.
     */
    private fun findRootViewFor(targetView: View): ViewGroup? {
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
        if (lastFrameLayout != null) return lastFrameLayout
        val activity = activityRef?.get() ?: return null
        return activity.findViewById(android.R.id.content)
    }

    private fun ensureKeyboardView(targetView: View? = null) {
        if (keyboardView != null) return

        // Find the content area (android.R.id.content) — this is where we apply padding
        val contentView = if (targetView != null) {
            findRootViewFor(targetView)
        } else {
            val activity = activityRef?.get() ?: (context as? Activity) ?: return
            activity.findViewById<ViewGroup>(android.R.id.content)
        } ?: return

        // contentRoot is the content area — padding goes here to push app content up
        contentRoot = contentView

        // The keyboard goes into the DecorView (parent of content) so it stays
        // at the absolute window bottom, unaffected by content padding
        val decorView = (contentView.rootView as? ViewGroup) ?: return

        val viewContext = contentView.context
        val density = viewContext.resources.displayMetrics.density

        // Calculate keyboard height up-front (available immediately)
        val navBarHeight = getNavigationBarHeight(contentView)
        keyboardHeightPx = (KEYBOARD_HEIGHT_DP * density).toInt() + navBarHeight

        val view = SecureKeyboardView(viewContext)
        view.renderer.setDensity(density)
        view.bottomInsetPx = navBarHeight.toFloat()

        // Host wraps an optional action strip + the keyboard so they move/show as a unit.
        val host = LinearLayout(viewContext).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val strip = KeyboardActionStrip(viewContext)
        host.addView(
            strip,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                actionStripHeightPx()
            )
        )
        strip.visibility = View.GONE

        host.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                keyboardHeightPx
            )
        )

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            totalKeyboardHeightPx()
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }

        // Add host (strip + keyboard) to DecorView — separate from content area
        decorView.addView(host, params)
        keyboardView = view
        keyboardHost = host
        actionStrip = strip

        // Listen for inset changes (rotation, nav bar changes)
        ViewCompat.setOnApplyWindowInsetsListener(host) { _, insets ->
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.bottomInsetPx = navInset.bottom.toFloat()
            val newKeyboardHeight = (KEYBOARD_HEIGHT_DP * density).toInt() + navInset.bottom
            keyboardHeightPx = newKeyboardHeight
            val kbLp = view.layoutParams as LinearLayout.LayoutParams
            kbLp.height = newKeyboardHeight
            view.layoutParams = kbLp
            val hostLp = host.layoutParams as FrameLayout.LayoutParams
            hostLp.height = totalKeyboardHeightPx()
            host.layoutParams = hostLp
            if (isKeyboardVisible) {
                val root = contentRoot
                if (root != null) {
                    root.setPadding(
                        root.paddingLeft,
                        root.paddingTop,
                        root.paddingRight,
                        originalContentBottomPadding + totalKeyboardHeightPx()
                    )
                }
                keyboardStateListener?.onKeyboardShown(totalKeyboardHeightPx())
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
        private const val SHOW_ANIMATION_DURATION = 250L
        private const val DISMISS_ANIMATION_DURATION = 200L

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
