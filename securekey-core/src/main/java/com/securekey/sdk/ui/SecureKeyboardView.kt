package com.securekey.sdk.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.securekey.sdk.core.Key
import com.securekey.sdk.core.KeyboardLayout
import com.securekey.sdk.core.KeyboardMode

/**
 * Custom View that renders the keyboard entirely via Canvas.
 * No child Views — all keys are drawn directly for security.
 * Delegates drawing to [KeyboardRenderer] and touch to [TouchHandler].
 */
class SecureKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val renderer = KeyboardRenderer()
    val touchHandler = TouchHandler(this)
    val animator = KeyboardAnimator(this)
    val keyPreview = KeyPreviewPopup()

    private var currentLayout: KeyboardLayout? = null
    /** Extra bottom padding in px to avoid navigation bar overlap */
    var bottomInsetPx: Float = 0f
        set(value) {
            field = value
            recalculateBounds()
        }

    /** Listener for key press events */
    var onKeyPressed: ((Key) -> Unit)? = null
        set(value) {
            field = value
            touchHandler.onKeyPressed = value
        }

    /** Listener for keyboard mode changes */
    var onModeChanged: ((KeyboardMode) -> Unit)? = null

    init {
        renderer.setDensity(resources.displayMetrics.density)

        touchHandler.onKeyDown = { key ->
            renderer.setPressedKey(key)
            keyPreview.show(key)
            invalidate()
        }

        touchHandler.onKeyUp = { key ->
            renderer.setPressedKey(null)
            keyPreview.hide()
            invalidate()
        }

        isFocusable = false
        isFocusableInTouchMode = false

        // Software rendering for setShadowLayer support
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /** Set the keyboard layout to render */
    fun setLayout(layout: KeyboardLayout) {
        currentLayout = layout.calculateBounds(
            width.toFloat(),
            height.toFloat(),
            resources.displayMetrics.density * 2.5f,
            bottomInsetPx
        )
        touchHandler.setLayout(currentLayout!!)
        invalidate()
    }

    /** Update only the labels (e.g. for shift) without re-generating the full layout */
    fun updateLabels(layout: KeyboardLayout) {
        currentLayout = layout
        touchHandler.setLayout(layout)
        invalidate()
    }

    private fun recalculateBounds() {
        currentLayout?.let { layout ->
            currentLayout = layout.calculateBounds(
                width.toFloat(),
                height.toFloat(),
                resources.displayMetrics.density * 2.5f,
                bottomInsetPx
            )
            touchHandler.setLayout(currentLayout!!)
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentLayout?.let { layout ->
            currentLayout = layout.calculateBounds(
                w.toFloat(),
                h.toFloat(),
                resources.displayMetrics.density * 2.5f,
                bottomInsetPx
            )
            touchHandler.setLayout(currentLayout!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layout = currentLayout ?: return
        renderer.draw(canvas, layout)
        val animating = keyPreview.draw(canvas, resources.displayMetrics.density)
        if (animating) {
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return touchHandler.onTouchEvent(event) || super.onTouchEvent(event)
    }

    /** Show the keyboard with animation */
    fun show() {
        visibility = VISIBLE
        animator.animateShow()
    }

    /** Dismiss the keyboard with animation */
    fun dismiss(onComplete: (() -> Unit)? = null) {
        animator.animateDismiss {
            visibility = GONE
            onComplete?.invoke()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        touchHandler.destroy()
        animator.cancel()
    }
}
