package com.securekey.sdk.security

/**
 * Security enforcement level for threat detection.
 */
enum class SecurityLevel {
    /** Block input and show warning on any threat */
    STRICT,
    /** Warn but allow user to continue */
    MODERATE,
    /** Log silently, no user-visible action */
    RELAXED
}
