package com.securekey.sdk.bridge

import android.webkit.JavascriptInterface
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * JavaScript bridge for WebView integration.
 * Exposes `window.SecureKey` interface.
 *
 * Usage in WebView:
 * ```kotlin
 * webView.addJavascriptInterface(WebViewBridge(secureKey), "SecureKey")
 * ```
 */
class WebViewBridge(private val secureKey: SecureKey) {

    @JavascriptInterface
    fun show() {
        secureKey.show()
    }

    @JavascriptInterface
    fun dismiss() {
        secureKey.dismiss()
    }

    @JavascriptInterface
    fun getSecurityReport(): String {
        val report = secureKey.getSecurityReport()
        return JSONObject().apply {
            put("isSecure", report.isSecure)
            put("activeKeyloggers", JSONArray(report.activeKeyloggers))
            put("hasScreenOverlay", report.hasScreenOverlay)
            put("isScreenBeingRecorded", report.isScreenBeingRecorded)
            put("threats", JSONArray(report.detectedThreats.map { it.name }))
        }.toString()
    }
}
