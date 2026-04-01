package com.securekey.sdk.security

import org.junit.Assert.*
import org.junit.Test

class IntegrityGuardTest {

    @Test
    fun `ThreatType enum has all expected values`() {
        val types = ThreatType.values()
        assertEquals(3, types.size)
        assertTrue(types.contains(ThreatType.ACCESSIBILITY_KEYLOGGER))
        assertTrue(types.contains(ThreatType.SCREEN_OVERLAY))
        assertTrue(types.contains(ThreatType.SCREEN_RECORDING))
    }

    @Test
    fun `SecurityLevel enum has all levels`() {
        val levels = SecurityLevel.values()
        assertEquals(3, levels.size)
        assertTrue(levels.contains(SecurityLevel.STRICT))
        assertTrue(levels.contains(SecurityLevel.MODERATE))
        assertTrue(levels.contains(SecurityLevel.RELAXED))
    }

    @Test
    fun `IntegrityReport detects threats correctly`() {
        val report = IntegrityReport(
            activeKeyloggers = listOf("com.example.keylogger"),
            hasScreenOverlay = true,
            isScreenBeingRecorded = false
        )
        assertEquals(2, report.detectedThreats.size)
        assertTrue(report.detectedThreats.contains(ThreatType.ACCESSIBILITY_KEYLOGGER))
        assertTrue(report.detectedThreats.contains(ThreatType.SCREEN_OVERLAY))
        assertFalse(report.isSecure)
    }

    @Test
    fun `IntegrityReport is secure when no threats`() {
        val report = IntegrityReport(
            activeKeyloggers = emptyList(),
            hasScreenOverlay = false,
            isScreenBeingRecorded = false
        )
        assertTrue(report.isSecure)
        assertTrue(report.detectedThreats.isEmpty())
    }

    @Test
    fun `IntegrityReport detects screen recording`() {
        val report = IntegrityReport(
            activeKeyloggers = emptyList(),
            hasScreenOverlay = false,
            isScreenBeingRecorded = true
        )
        assertEquals(1, report.detectedThreats.size)
        assertTrue(report.detectedThreats.contains(ThreatType.SCREEN_RECORDING))
        assertFalse(report.isSecure)
    }
}
