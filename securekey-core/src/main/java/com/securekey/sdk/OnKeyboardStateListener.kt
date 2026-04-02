package com.securekey.sdk

/**
 * Callback interface for keyboard visibility state changes.
 * Use this to adjust your layout when the secure keyboard appears or disappears.
 */
interface OnKeyboardStateListener {
    /**
     * Called when the keyboard becomes visible.
     * @param heightPx the total keyboard height in pixels (including navigation bar inset)
     */
    fun onKeyboardShown(heightPx: Int)

    /** Called when the keyboard is dismissed. */
    fun onKeyboardHidden()
}
