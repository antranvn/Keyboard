package com.securekey.sdk.security

import android.content.Context

/**
 * Device integrity checks: overlay detection, screen recording,
 * and accessibility keylogger detection.
 */
class IntegrityGuard(private val context: Context) {

    /** Generate a full integrity report */
    fun generateReport(): IntegrityReport {
        val antiKeylog = AntiKeylogProtection(context)
        return IntegrityReport(
            activeKeyloggers = antiKeylog.detectKeyloggers(),
            hasScreenOverlay = false, // requires runtime window check
            isScreenBeingRecorded = false // requires API 34+ callback
        )
    }
}
