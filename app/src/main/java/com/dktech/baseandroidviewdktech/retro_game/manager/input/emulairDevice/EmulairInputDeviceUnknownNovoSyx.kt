package com.emulator.retro.console.game.retro_game.manager.input.emulairDevice

import android.content.Context
import com.emulator.retro.console.game.retro_game.manager.input.PauseMenuShortcutNovoSyx
import com.emulator.retro.console.game.retro_game.utils.input.InputKeyNovoSyx
import com.emulator.retro.console.game.retro_game.utils.input.RetroKeyNovoSyx

object EmulairInputDeviceUnknownNovoSyx : EmulairInputDevice {
    override fun getDefaultBindings(): Map<InputKeyNovoSyx, RetroKeyNovoSyx> = emptyMap()

    override fun isSupported(): Boolean = false

    override fun isEnabledByDefault(appContext: Context): Boolean = false

    override fun getSupportedShortcuts(): List<PauseMenuShortcutNovoSyx> = emptyList()

    override fun getCustomizableKeys(): List<RetroKeyNovoSyx> = emptyList()
}
