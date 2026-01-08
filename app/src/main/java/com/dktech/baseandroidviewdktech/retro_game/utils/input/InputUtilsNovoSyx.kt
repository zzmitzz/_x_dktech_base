package com.emulator.retro.console.game.retro_game.utils.input

import android.view.InputDevice

internal fun inputKeySetOf(vararg keyCodes: Int) = inputKeysOf(*keyCodes).toSet()

internal fun inputKeysOf(vararg keyCodes: Int) = keyCodes.map(::InputKeyNovoSyx)

internal fun retroKeysOf(vararg keyCodes: Int) = keyCodes.map(::RetroKeyNovoSyx)

internal fun bindingsOf(vararg bindings: Pair<Int, Int>) = bindings.associate {
    InputKeyNovoSyx(it.first) to RetroKeyNovoSyx(it.second)
}

fun InputDevice.supportsAllKeys(inputKeys: List<InputKeyNovoSyx>): Boolean {
    val supportedKeyCodes = inputKeys
        .map { it.keyCode }
        .toIntArray()

    return this.hasKeys(*supportedKeyCodes).all { it }
}
