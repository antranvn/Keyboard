package com.securekey.sdk.security

/**
 * Report containing results of all security checks.
 */
data class IntegrityReport(
    val activeKeyloggers: List<String>,
    val hasScreenOverlay: Boolean,
    val isScreenBeingRecorded: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Returns all detected threats */
    val detectedThreats: List<ThreatType>
        get() = buildList {
            if (activeKeyloggers.isNotEmpty()) add(ThreatType.ACCESSIBILITY_KEYLOGGER)
            if (hasScreenOverlay) add(ThreatType.SCREEN_OVERLAY)
            if (isScreenBeingRecorded) add(ThreatType.SCREEN_RECORDING)
        }

    /** Whether the device is considered secure */
    val isSecure: Boolean
        get() = detectedThreats.isEmpty()
}
