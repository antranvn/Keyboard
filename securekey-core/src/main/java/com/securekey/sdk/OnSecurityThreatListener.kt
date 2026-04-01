package com.securekey.sdk

import com.securekey.sdk.security.ThreatType

/**
 * Callback interface for security threat notifications.
 */
fun interface OnSecurityThreatListener {
    /**
     * Called when a security threat is detected.
     * @param threat the type of threat
     * @param details additional information (e.g., package name of keylogger)
     */
    fun onThreatDetected(threat: ThreatType, details: String)
}
