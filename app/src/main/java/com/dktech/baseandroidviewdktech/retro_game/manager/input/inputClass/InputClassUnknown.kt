package com.emulator.retro.console.game.retro_game.manager.input.inputClass

import com.emulator.retro.console.game.retro_game.utils.input.InputKeyNovoSyx

object InputClassUnknown : InputClass {
    override fun getInputKeys(): Set<InputKeyNovoSyx> {
        return emptySet()
    }

    override fun getAxesMap(): Map<Int, Int> {
        return emptyMap()
    }
}
