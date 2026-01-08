package com.emulator.retro.console.game.retro_game.manager.input.inputClass

import android.view.InputDevice
import com.emulator.retro.console.game.retro_game.utils.input.InputKeyNovoSyx

interface InputClass {

    fun getInputKeys(): Set<InputKeyNovoSyx>

    fun getAxesMap(): Map<Int, Int>
}

fun InputDevice?.getInputClass(): InputClass {
    return when {
        this == null -> InputClassUnknown
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> InputClassGamePad
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> InputClassKeyboard
        else -> InputClassUnknown
    }
}
