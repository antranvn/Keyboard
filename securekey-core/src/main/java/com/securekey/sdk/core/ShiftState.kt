package com.securekey.sdk.core

/**
 * Shift key state machine: OFF -> SINGLE -> CAPS_LOCK.
 * Auto-resets after a character is typed in SINGLE mode.
 */
class ShiftState {

    enum class State {
        OFF, SINGLE, CAPS_LOCK
    }

    var state: State = State.OFF
        private set

    /** Whether shift is currently active (SINGLE or CAPS_LOCK) */
    val isActive: Boolean
        get() = state != State.OFF

    /** Whether caps lock is on */
    val isCapsLock: Boolean
        get() = state == State.CAPS_LOCK

    /** Toggle shift: OFF -> SINGLE -> CAPS_LOCK -> OFF */
    fun toggle() {
        state = when (state) {
            State.OFF -> State.SINGLE
            State.SINGLE -> State.CAPS_LOCK
            State.CAPS_LOCK -> State.OFF
        }
    }

    /** Called after a character is typed. Resets SINGLE to OFF. */
    fun onCharacterTyped() {
        if (state == State.SINGLE) {
            state = State.OFF
        }
    }

    /** Reset to OFF */
    fun reset() {
        state = State.OFF
    }
}
