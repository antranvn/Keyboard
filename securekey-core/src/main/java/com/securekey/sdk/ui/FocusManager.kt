package com.securekey.sdk.ui

import android.os.Handler
import android.os.Looper
import android.view.View
import com.securekey.sdk.core.KeyboardMode

/**
 * Monitors attached EditTexts for focus changes.
 * Shows the correct keyboard mode on focus gain. Defers dismiss
 * so that rapid focus moves between registered fields don't cause
 * the keyboard to bounce (dismiss + re-show).
 */
class FocusManager {

    private val registeredFields = mutableMapOf<View, KeyboardMode>()
    private var onShowKeyboard: ((View, KeyboardMode) -> Unit)? = null
    private var onDismissKeyboard: (() -> Unit)? = null
    private var currentFocusedView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingDismiss: Runnable? = null

    private val focusListener = View.OnFocusChangeListener { view, hasFocus ->
        if (hasFocus && registeredFields.containsKey(view)) {
            // Cancel any pending dismiss — focus moved to another registered field
            cancelPendingDismiss()
            val mode = registeredFields[view]!!
            currentFocusedView = view
            onShowKeyboard?.invoke(view, mode)
        } else if (!hasFocus && view == currentFocusedView) {
            // Defer dismiss to give the next field time to gain focus
            scheduleDismiss()
        }
    }

    private fun scheduleDismiss() {
        cancelPendingDismiss()
        val runnable = Runnable {
            // Re-check: if a registered field now has focus, don't dismiss
            val newFocused = registeredFields.keys.firstOrNull { it.hasFocus() }
            if (newFocused == null) {
                currentFocusedView = null
                onDismissKeyboard?.invoke()
            }
            pendingDismiss = null
        }
        pendingDismiss = runnable
        handler.postDelayed(runnable, 80)
    }

    private fun cancelPendingDismiss() {
        pendingDismiss?.let { handler.removeCallbacks(it) }
        pendingDismiss = null
    }

    /** Register an EditText to be managed */
    fun register(view: View, mode: KeyboardMode) {
        registeredFields[view] = mode
        view.onFocusChangeListener = focusListener
    }

    /** Unregister an EditText */
    fun unregister(view: View) {
        registeredFields.remove(view)
        if (view == currentFocusedView) {
            currentFocusedView = null
        }
    }

    /** Set callback for showing keyboard */
    fun setOnShowKeyboard(listener: (View, KeyboardMode) -> Unit) {
        onShowKeyboard = listener
    }

    /** Set callback for dismissing keyboard */
    fun setOnDismissKeyboard(listener: () -> Unit) {
        onDismissKeyboard = listener
    }

    /** Get the currently focused field */
    fun getCurrentFocusedView(): View? = currentFocusedView

    /** Clean up all registrations */
    fun destroy() {
        cancelPendingDismiss()
        handler.removeCallbacksAndMessages(null)
        registeredFields.clear()
        currentFocusedView = null
    }
}
