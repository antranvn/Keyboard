package com.securekey.sdk.ui

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatEditText

/**
 * Secure input field that disables copy/paste/cut/share/autofill,
 * text selection handles, long-press menu, suggestions, and
 * sets FLAG_SECURE to prevent screen capture of input content.
 */
open class SecureEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /** Block all clipboard and text manipulation actions */
    private val blockingActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false
        override fun onDestroyActionMode(mode: ActionMode?) {}
    }

    init {
        inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        customSelectionActionModeCallback = blockingActionModeCallback
        customInsertionActionModeCallback = blockingActionModeCallback
        setTextIsSelectable(false)
        isLongClickable = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        showSoftInputOnFocus = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (context as? android.app.Activity)?.window?.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun isSuggestionsEnabled(): Boolean = false
}
