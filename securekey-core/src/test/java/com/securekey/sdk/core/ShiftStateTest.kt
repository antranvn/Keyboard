package com.securekey.sdk.core

import org.junit.Assert.*
import org.junit.Test

class ShiftStateTest {

    @Test
    fun `initial state is OFF`() {
        val state = ShiftState()
        assertEquals(ShiftState.State.OFF, state.state)
        assertFalse(state.isActive)
        assertFalse(state.isCapsLock)
    }

    @Test
    fun `toggle cycles through states`() {
        val state = ShiftState()
        state.toggle()
        assertEquals(ShiftState.State.SINGLE, state.state)
        assertTrue(state.isActive)
        assertFalse(state.isCapsLock)

        state.toggle()
        assertEquals(ShiftState.State.CAPS_LOCK, state.state)
        assertTrue(state.isActive)
        assertTrue(state.isCapsLock)

        state.toggle()
        assertEquals(ShiftState.State.OFF, state.state)
        assertFalse(state.isActive)
    }

    @Test
    fun `SINGLE resets on character typed`() {
        val state = ShiftState()
        state.toggle() // SINGLE
        state.onCharacterTyped()
        assertEquals(ShiftState.State.OFF, state.state)
    }

    @Test
    fun `CAPS_LOCK does not reset on character typed`() {
        val state = ShiftState()
        state.toggle() // SINGLE
        state.toggle() // CAPS_LOCK
        state.onCharacterTyped()
        assertEquals(ShiftState.State.CAPS_LOCK, state.state)
    }

    @Test
    fun `reset goes to OFF`() {
        val state = ShiftState()
        state.toggle()
        state.toggle()
        state.reset()
        assertEquals(ShiftState.State.OFF, state.state)
    }
}
