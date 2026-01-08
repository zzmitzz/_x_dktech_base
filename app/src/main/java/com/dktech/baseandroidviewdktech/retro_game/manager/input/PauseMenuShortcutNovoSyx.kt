package com.emulator.retro.console.game.retro_game.manager.input

import android.view.InputDevice
import com.emulator.retro.console.game.retro_game.manager.input.emulairDevice.getEmulairInputDevice

data class PauseMenuShortcutNovoSyx(val name: String, val keys: Set<Int>) {

    companion object {

        fun getDefault(inputDevice: InputDevice): PauseMenuShortcutNovoSyx? {
            return inputDevice.getEmulairInputDevice()
                .getSupportedShortcuts()
                .firstOrNull { shortcut ->
                    inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it }
                }
        }

        fun findByName(device: InputDevice, name: String): PauseMenuShortcutNovoSyx? {
            return device.getEmulairInputDevice()
                .getSupportedShortcuts()
                .firstOrNull { it.name == name }
        }
    }
}
