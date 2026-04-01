package com.securekey.sdk.ui

import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyType
import com.securekey.sdk.core.KeyboardLayout

/**
 * Handles touch events for the keyboard view.
 * Hit-tests against key bounds, supports key repeat for backspace.
 */
class TouchHandler(private val view: View) {

    var onKeyPressed: ((Key) -> Unit)? = null
    var onKeyDown: ((Key) -> Unit)? = null
    var onKeyUp: ((Key) -> Unit)? = null
    var hapticEnabled: Boolean = true

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

    private var currentKey: Key? = null
    private var layout: KeyboardLayout? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val repeatInitialDelay = 300L
    private val repeatInterval = 50L

    private val repeatRunnable = object : Runnable {
        override fun run() {
            currentKey?.let { key ->
                onKeyPressed?.invoke(key)
                performHaptic()
                handler.postDelayed(this, repeatInterval)
            }
        }
    }

    /** Update the current layout for hit-testing */
    fun setLayout(layout: KeyboardLayout) {
        this.layout = layout
    }

    /** Process a touch event, return true if handled */
    fun onTouchEvent(event: MotionEvent): Boolean {
        val currentLayout = layout ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTouchY = event.y
                val key = currentLayout.findKeyAt(event.x, event.y)
                if (key != null) {
                    currentKey = key
                    onKeyDown?.invoke(key)
                    performHaptic()

                    // Start repeat for backspace
                    if (key.value == "BACKSPACE") {
                        handler.postDelayed(repeatRunnable, repeatInitialDelay)
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - initialTouchX
                val dy = event.y - initialTouchY
                if (dx * dx + dy * dy > touchSlop * touchSlop) {
                    val newKey = currentLayout.findKeyAt(event.x, event.y)
                    if (newKey != currentKey) {
                        currentKey?.let { onKeyUp?.invoke(it) }
                        handler.removeCallbacks(repeatRunnable)
                        currentKey = newKey
                        newKey?.let { onKeyDown?.invoke(it) }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(repeatRunnable)
                currentKey?.let { key ->
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        onKeyPressed?.invoke(key)
                    }
                    onKeyUp?.invoke(key)
                }
                currentKey = null
                return true
            }
        }
        return false
    }

    private fun performHaptic() {
        if (hapticEnabled) {
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    /** Clean up resources */
    fun destroy() {
        handler.removeCallbacksAndMessages(null)
    }
}
