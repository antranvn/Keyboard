package com.securekey.sdk.ui

import android.animation.ValueAnimator
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

/**
 * Handles keyboard show/dismiss and layout switch animations.
 * Respects system animation scale settings.
 */
class KeyboardAnimator(private val view: View) {

    private var showAnimator: ValueAnimator? = null
    private var dismissAnimator: ValueAnimator? = null

    private val showDuration = 300L
    private val dismissDuration = 250L
    private val crossfadeDuration = 200L

    /** Slide-up show animation */
    fun animateShow(onUpdate: ((Float) -> Unit)? = null) {
        dismissAnimator?.cancel()

        val scale = getAnimationScale()
        if (scale == 0f) {
            view.translationY = 0f
            view.alpha = 1f
            onUpdate?.invoke(1f)
            return
        }

        view.translationY = view.height.toFloat()
        view.alpha = 0f

        showAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (showDuration * scale).toLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                view.translationY = view.height * (1f - progress)
                view.alpha = progress
                onUpdate?.invoke(progress)
            }
            start()
        }
    }

    /** Slide-down dismiss animation */
    fun animateDismiss(onComplete: (() -> Unit)? = null) {
        showAnimator?.cancel()

        val scale = getAnimationScale()
        if (scale == 0f) {
            view.translationY = view.height.toFloat()
            view.alpha = 0f

            onComplete?.invoke()
            return
        }

        dismissAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (dismissDuration * scale).toLong()
            interpolator = AccelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                view.translationY = view.height * progress
                view.alpha = 1f - progress
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
            start()
        }
    }

    /** Crossfade animation for layout switches */
    fun animateLayoutSwitch(onMidpoint: () -> Unit) {
        val scale = getAnimationScale()
        if (scale == 0f) {
            onMidpoint()
            return
        }

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (crossfadeDuration * scale).toLong()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                if (progress >= 0.5f) {
                    view.alpha = progress
                } else {
                    view.alpha = 1f - progress
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    view.alpha = 1f
                }
            })
            start()

            // Call midpoint at 50%
            view.postDelayed({
                onMidpoint()
            }, (crossfadeDuration * scale / 2).toLong())
        }
    }

    private fun getAnimationScale(): Float {
        return try {
            Settings.Global.getFloat(
                view.context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        } catch (_: Exception) {
            1f
        }
    }

    /** Cancel all running animations */
    fun cancel() {
        showAnimator?.cancel()
        dismissAnimator?.cancel()
    }
}
