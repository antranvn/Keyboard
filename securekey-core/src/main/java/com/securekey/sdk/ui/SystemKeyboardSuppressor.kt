package com.securekey.sdk.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Suppresses the system soft keyboard from appearing on secure fields.
 * Handles vendor-specific quirks (e.g., Samsung aggressive IME show).
 */
class SystemKeyboardSuppressor(private val context: Context) {

    private val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private val handler = Handler(Looper.getMainLooper())
    private val suppressedViews = mutableSetOf<View>()

    /** Suppress system keyboard for the given EditText */
    fun suppress(editText: EditText) {
        editText.showSoftInputOnFocus = false
        suppressedViews.add(editText)

        // Hide immediately
        hideSystemKeyboard(editText)

        // Samsung workaround: some devices re-show IME after a short delay
        handler.postDelayed({
            if (editText.hasFocus()) {
                hideSystemKeyboard(editText)
            }
        }, 50)
    }

    /** Restore system keyboard for the given EditText */
    fun restore(editText: EditText) {
        editText.showSoftInputOnFocus = true
        suppressedViews.remove(editText)
    }

    /** Hide the system keyboard */
    fun hideSystemKeyboard(view: View) {
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /** Re-suppress after rotation or configuration change */
    fun onConfigurationChanged() {
        handler.postDelayed({
            suppressedViews.forEach { view ->
                if (view is EditText && view.hasFocus()) {
                    hideSystemKeyboard(view)
                }
            }
        }, 100)
    }

    /** Clean up */
    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        suppressedViews.clear()
    }
}
