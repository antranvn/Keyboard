package com.securekey.sdk.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Thin strip rendered above the keyboard that exposes a single labelled action
 * to the host app (e.g. "Sign in with saved password"). Styled like the system
 * keyboard's inline suggestion chip: centered pill with a subtle border.
 */
internal class KeyboardActionStrip(context: Context) : LinearLayout(context) {

    private val chip = LinearLayout(context)
    private val iconView = ImageView(context)
    private val labelView = TextView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.parseColor("#F5F5F7"))

        val density = resources.displayMetrics.density
        val chipPadH = (16 * density).toInt()
        val chipPadV = (6 * density).toInt()

        chip.orientation = HORIZONTAL
        chip.gravity = Gravity.CENTER_VERTICAL
        chip.isClickable = true
        chip.isFocusable = true
        chip.setPadding(chipPadH, chipPadV, chipPadH, chipPadV)

        val radius = (18 * density)
        val border = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.WHITE)
            setStroke((1 * density).toInt(), Color.parseColor("#D0D0D5"))
        }
        chip.background = RippleDrawable(
            ColorStateList.valueOf(Color.parseColor("#1F1A73E8")),
            border,
            null
        )

        val iconSize = (18 * density).toInt()
        val iconParams = LayoutParams(iconSize, iconSize).apply {
            marginEnd = (8 * density).toInt()
        }
        iconView.layoutParams = iconParams
        iconView.visibility = View.GONE
        chip.addView(iconView)

        labelView.setTextColor(Color.parseColor("#1A73E8"))
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        labelView.isAllCaps = false
        labelView.maxLines = 1
        chip.addView(labelView)

        addView(
            chip,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        )
    }

    fun bind(label: String, iconResId: Int?, onClick: () -> Unit) {
        labelView.text = label
        if (iconResId != null) {
            iconView.setImageResource(iconResId)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = View.GONE
        }
        chip.setOnClickListener { onClick() }
    }

    companion object {
        const val HEIGHT_DP = 44
    }
}
