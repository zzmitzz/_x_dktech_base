package com.emulator.retro.console.game.retro_game.launcher

import android.content.Context
import android.content.Intent
import com.bigbratan.emulair.common.managers.coresLibrary.CoresSelection
import com.bigbratan.emulair.common.metadata.retrograde.GameSystem
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.emulator.retro.console.game.retro_game.activities.BaseGameActivityNovoSyx

class GameLauncherNovoSyx(
    private val coresSelection: CoresSelection,
    private val gameLaunchTaskHandler: GameLaunchTaskHandlerNovoSyx
) {

    suspend fun launchGameAsync(mContext: Context, game: Game, loadSave: Boolean): Intent {
        val system = GameSystem.findById(game.systemId)
        val coreConfig = coresSelection.getCoreConfigForSystem(system)
        gameLaunchTaskHandler.handleGameStart(mContext.applicationContext)
        return BaseGameActivityNovoSyx.launchGame(mContext, coreConfig, game, loadSave)
    }
}
