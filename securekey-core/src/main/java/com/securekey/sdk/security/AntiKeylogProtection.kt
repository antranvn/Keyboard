package com.securekey.sdk.security

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Detects accessibility services that have key event filtering capability,
 * which could be used for keylogging.
 */
class AntiKeylogProtection(private val context: Context) {

    /**
     * Returns list of package names of accessibility services that have
     * [AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS] enabled.
     */
    fun detectKeyloggers(): List<String> {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return emptyList()

        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .filter { it.flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS != 0 }
            .mapNotNull { it.resolveInfo?.serviceInfo?.packageName }
    }

    /** Check if any keylogger-capable services are active */
    fun hasActiveKeyloggers(): Boolean = detectKeyloggers().isNotEmpty()
}
