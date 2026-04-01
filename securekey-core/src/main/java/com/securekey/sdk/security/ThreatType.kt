package com.securekey.sdk.security

/**
 * Types of security threats that can be detected by the SDK.
 */
enum class ThreatType {
    /** An accessibility service with key event filtering capability is active */
    ACCESSIBILITY_KEYLOGGER,
    /** A screen overlay is detected on top of the keyboard */
    SCREEN_OVERLAY,
    /** Screen recording or screen capture is active */
    SCREEN_RECORDING
}
