package com.securekey.sdk.core

/**
 * Available keyboard modes.
 */
enum class KeyboardMode {
    /** Full QWERTY keyboard with letters, numbers, and symbols */
    QWERTY_FULL,
    /** Numeric PIN entry pad */
    NUMERIC_PIN,
    /** Numeric OTP entry pad with auto-advance */
    NUMERIC_OTP,
    /** Amount entry pad with decimal and currency support */
    AMOUNT_PAD
}
