package com.securekey.sdk.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Guards against clipboard-based data exfiltration.
 * Blocks copy/cut/paste on secure fields and clears clipboard on activation.
 */
class ClipboardGuard(private val context: Context) {

    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    /** Clear the system clipboard */
    fun clearClipboard() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        cm.setPrimaryClip(ClipData.newPlainText("", ""))
    }

    /** Start monitoring clipboard for changes while secure input is active */
    fun startMonitoring(onClipboardAccess: () -> Unit) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        stopMonitoring()
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            onClipboardAccess()
        }
        cm.addPrimaryClipChangedListener(clipboardListener)
    }

    /** Stop clipboard monitoring */
    fun stopMonitoring() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboardListener?.let { cm.removePrimaryClipChangedListener(it) }
        clipboardListener = null
    }
}
