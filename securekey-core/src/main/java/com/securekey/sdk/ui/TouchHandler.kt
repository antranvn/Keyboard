package com.securekey.sdk.ui

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyType
import com.securekey.sdk.core.KeyboardLayout
import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Handles touch events for the keyboard view.
 * Hit-tests against key bounds, supports key repeat for backspace,
 * and — when [swipeEnabled] — detects glide/swipe gestures that
 * emit a sequence of character keys based on pivot detection.
 */
class TouchHandler(private val view: View) {

    var onKeyPressed: ((Key) -> Unit)? = null
    var onKeyDown: ((Key) -> Unit)? = null
    var onKeyUp: ((Key) -> Unit)? = null

    /** Called with the ordered list of character keys collected along a swipe. */
    var onSwipeCommitted: ((List<Key>) -> Unit)? = null

    /** Called during a swipe with the current path for trail rendering. */
    var onSwipePath: ((List<PointF>) -> Unit)? = null

    /** Enable swipe/glide typing. When false, behavior matches plain tap mode. */
    var swipeEnabled: Boolean = false

    var hapticEnabled: Boolean = true

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
    private val density = view.resources.displayMetrics.density

    private var currentKey: Key? = null
    private var layout: KeyboardLayout? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    // Swipe state
    private var isSwiping = false
    private var swipeStartKey: Key? = null
    private val sampledPoints = ArrayList<PointF>(128)
    private val pivotKeys = ArrayList<Key>(16)
    private var lastSampleTime = 0L
    private var lastKeyEnterTime = 0L
    private var lastKeyForDwell: Key? = null

    private val repeatInitialDelay = 300L
    private val repeatInterval = 50L

    // Swipe tuning
    private val swipeActivationDistance = touchSlop * 2f
    private val pivotAngleThresholdRad = Math.toRadians(60.0)
    private val dwellMs = 120L
    private val sampleIntervalMs = 10L
    private val sampleDistancePx = 4f * density

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
                isSwiping = false
                sampledPoints.clear()
                pivotKeys.clear()
                val key = currentLayout.findKeyAt(event.x, event.y)
                if (key != null) {
                    currentKey = key
                    swipeStartKey = key
                    lastKeyForDwell = key
                    lastKeyEnterTime = event.eventTime
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
                val distSq = dx * dx + dy * dy

                if (!isSwiping) {
                    // Decide whether to enter swipe mode
                    val startKey = swipeStartKey
                    val canSwipe = swipeEnabled &&
                        startKey != null &&
                        startKey.type == KeyType.CHARACTER &&
                        distSq > swipeActivationDistance * swipeActivationDistance

                    if (canSwipe) {
                        beginSwipe(event)
                        return true
                    }

                    // Plain-mode behavior: update pressed-key highlight on cross
                    if (distSq > touchSlop * touchSlop) {
                        val newKey = currentLayout.findKeyAt(event.x, event.y)
                        if (newKey != currentKey) {
                            currentKey?.let { onKeyUp?.invoke(it) }
                            handler.removeCallbacks(repeatRunnable)
                            currentKey = newKey
                            newKey?.let { onKeyDown?.invoke(it) }
                        }
                    }
                } else {
                    handleSwipeMove(event, currentLayout)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(repeatRunnable)

                if (isSwiping) {
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        finalizeSwipe(event, currentLayout)
                    } else {
                        cancelSwipe()
                    }
                    currentKey = null
                    return true
                }

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

    private fun beginSwipe(event: MotionEvent) {
        isSwiping = true
        // Clear tap highlight — swipe has its own trail visualization
        currentKey?.let { onKeyUp?.invoke(it) }
        currentKey = null

        sampledPoints.clear()
        pivotKeys.clear()
        sampledPoints.add(PointF(initialTouchX, initialTouchY))
        sampledPoints.add(PointF(event.x, event.y))
        swipeStartKey?.let { pivotKeys.add(it) }
        lastSampleTime = event.eventTime
        onSwipePath?.invoke(sampledPoints)
    }

    private fun handleSwipeMove(event: MotionEvent, layout: KeyboardLayout) {
        val now = event.eventTime
        val last = sampledPoints.lastOrNull() ?: return
        val dist = hypot(event.x - last.x, event.y - last.y)
        if (now - lastSampleTime < sampleIntervalMs && dist < sampleDistancePx) return

        sampledPoints.add(PointF(event.x, event.y))
        lastSampleTime = now

        // Hit-test current position
        val keyUnderFinger = layout.findKeyAt(event.x, event.y)
        if (keyUnderFinger != null && keyUnderFinger.type == KeyType.CHARACTER) {
            // Dwell detection
            if (keyUnderFinger == lastKeyForDwell) {
                if (now - lastKeyEnterTime >= dwellMs) {
                    addPivotIfNew(keyUnderFinger)
                }
            } else {
                lastKeyForDwell = keyUnderFinger
                lastKeyEnterTime = now
            }

            // Angle-based pivot detection on the last 3 sampled points
            if (sampledPoints.size >= 3) {
                val a = sampledPoints[sampledPoints.size - 3]
                val b = sampledPoints[sampledPoints.size - 2]
                val c = sampledPoints[sampledPoints.size - 1]
                val angle = angleBetween(a, b, c)
                if (angle > pivotAngleThresholdRad) {
                    val keyAtB = layout.findKeyAt(b.x, b.y)
                    if (keyAtB != null && keyAtB.type == KeyType.CHARACTER) {
                        addPivotIfNew(keyAtB)
                    }
                }
            }
        }

        onSwipePath?.invoke(sampledPoints)
    }

    private fun finalizeSwipe(event: MotionEvent, layout: KeyboardLayout) {
        // Append terminal key
        val endKey = layout.findKeyAt(event.x, event.y)
        if (endKey != null && endKey.type == KeyType.CHARACTER) {
            addPivotIfNew(endKey)
        }

        val emitted = pivotKeys.toList()
        cancelSwipe()
        if (emitted.isNotEmpty()) {
            onSwipeCommitted?.invoke(emitted)
        }
    }

    private fun cancelSwipe() {
        isSwiping = false
        sampledPoints.clear()
        pivotKeys.clear()
        swipeStartKey = null
        lastKeyForDwell = null
        onSwipePath?.invoke(emptyList())
    }

    private fun addPivotIfNew(key: Key) {
        // Allow consecutive duplicates (e.g., double letter via dwell),
        // but skip if identical to the immediate previous pivot AND
        // no intervening different key has been pivoted since.
        if (pivotKeys.isNotEmpty() && pivotKeys.last() == key) return
        pivotKeys.add(key)
    }

    private fun angleBetween(a: PointF, b: PointF, c: PointF): Double {
        val v1x = (b.x - a.x).toDouble()
        val v1y = (b.y - a.y).toDouble()
        val v2x = (c.x - b.x).toDouble()
        val v2y = (c.y - b.y).toDouble()
        if ((v1x == 0.0 && v1y == 0.0) || (v2x == 0.0 && v2y == 0.0)) return 0.0
        val a1 = atan2(v1y, v1x)
        val a2 = atan2(v2y, v2x)
        var diff = a2 - a1
        while (diff > Math.PI) diff -= 2 * Math.PI
        while (diff < -Math.PI) diff += 2 * Math.PI
        return abs(diff)
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
