package com.emulator.retro.console.game.retro_game.manager.state.models

data class SaveStateModelNovoSyx(
    val mPreview: String,
    val mTitle: String,
    val mDateTime: Long
) {
    companion object {
        val none by lazy { SaveStateModelNovoSyx("none", "none", -1) }
    }
}