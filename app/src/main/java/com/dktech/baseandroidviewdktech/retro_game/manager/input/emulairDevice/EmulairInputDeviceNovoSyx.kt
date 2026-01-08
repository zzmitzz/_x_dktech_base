package com.emulator.retro.console.game.retro_game.manager.input.emulairDevice

import android.content.Context
import android.view.InputDevice
import com.emulator.retro.console.game.retro_game.manager.input.PauseMenuShortcutNovoSyx
import com.emulator.retro.console.game.retro_game.utils.input.InputKeyNovoSyx
import com.emulator.retro.console.game.retro_game.utils.input.RetroKeyNovoSyx

interface EmulairInputDevice {

    fun getCustomizableKeys(): List<RetroKeyNovoSyx>

    fun getDefaultBindings(): Map<InputKeyNovoSyx, RetroKeyNovoSyx>

    fun isSupported(): Boolean

    fun isEnabledByDefault(appContext: Context): Boolean

    fun getSupportedShortcuts(): List<PauseMenuShortcutNovoSyx>
}

fun InputDevice?.getEmulairInputDevice(): EmulairInputDevice {
    return when {
        this == null -> EmulairInputDeviceUnknownNovoSyx
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> EmulairInputDeviceGamePadNovoSyx(this)
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> EmulairInputDeviceKeyboardNovoSyx(this)
        else -> EmulairInputDeviceUnknownNovoSyx
    }
}
