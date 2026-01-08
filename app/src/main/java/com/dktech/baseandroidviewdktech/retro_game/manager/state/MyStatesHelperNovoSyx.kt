package com.emulator.retro.console.game.retro_game.manager.state

import com.bigbratan.emulair.common.managers.saves.StatesManager
import com.bigbratan.emulair.common.managers.saves.StatesPreviewManager
import com.bigbratan.emulair.common.metadata.retrograde.CoreID
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.emulator.retro.console.game.retro_game.manager.state.models.LoadStateModelNovoSyx
import com.emulator.retro.console.game.retro_game.manager.state.models.SaveStateModelNovoSyx
import com.emulator.retro.console.game.utils.SharePrefManagerNovoSyx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyStatesHelperNovoSyx(
    private val mStatesManager: StatesManager,
    private val mStatesPreviewManager: StatesPreviewManager
) {
    suspend fun getSaveStates(game: Game, coreID: CoreID): List<SaveStateModelNovoSyx> = withContext(Dispatchers.IO) {
        val saveInfos = mStatesManager.getSavedSlotsInfo(game, coreID)
        saveInfos.mapIndexed { index, saveInfo ->
            if (saveInfo.exists) {
                SaveStateModelNovoSyx(
                    mStatesPreviewManager.getPreviewForSlot(game, coreID, index),
                    getTitle(game, coreID, index),
                    getDateTime(game, coreID, index).takeIf { it != -1L } ?: saveInfo.date
                )
            } else {
                SaveStateModelNovoSyx.none
            }
        }
    }

    suspend fun getLoadStates(game: Game, coreID: CoreID): List<LoadStateModelNovoSyx> = withContext(Dispatchers.IO) {
        val saveInfos = mStatesManager.getSavedSlotsInfo(game, coreID)
        saveInfos.mapIndexed { index, saveInfo ->
            if (saveInfo.exists) {
                LoadStateModelNovoSyx(
                    index,
                    mStatesPreviewManager.getPreviewForSlot(game, coreID, index),
                    getTitle(game, coreID, index),
                    getDateTime(game, coreID, index).takeIf { it != -1L } ?: saveInfo.date
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    private fun getTitle(game: Game, coreID: CoreID, index: Int): String {
        val key = "${coreID.coreName}.${game.id}.$index.title"
        return SharePrefManagerNovoSyx.get(key, "State $index")!!
    }

    private fun getDateTime(game: Game, coreID: CoreID, index: Int): Long {
        val key = "${coreID.coreName}.${game.id}.$index.datetime"
        return SharePrefManagerNovoSyx.get(key, -1L)
    }

    fun saveTitleAndDate(game: Game, coreID: CoreID, iSlot: Int, name: String, time: Long) {
        val keyTitle = "${coreID.coreName}.${game.id}.$iSlot.title"
        val keyDateTime = "${coreID.coreName}.${game.id}.$iSlot.datetime"

        SharePrefManagerNovoSyx.put(keyTitle, name)
        SharePrefManagerNovoSyx.put(keyDateTime, time)
    }
}