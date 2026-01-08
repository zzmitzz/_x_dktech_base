package com.emulator.retro.console.game.retro_game.manager.input

import android.content.SharedPreferences
import com.bigbratan.emulair.common.managers.controller.ControllerConfig
import com.bigbratan.emulair.common.metadata.retrograde.CoreID
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.metadata.retrograde.SystemID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ControllerConfigsManagerNovoSyx(private val sharedPreferences: SharedPreferences) {

    suspend fun getControllerConfigs(
        systemId: SystemID,
        systemCoreConfig: SystemCoreConfig
    ): Map<Int, ControllerConfig> = withContext(Dispatchers.IO) {
        systemCoreConfig.controllerConfigs.entries
            .associate { (port, controllers) ->
                val currentName = sharedPreferences.getString(
                    getSharedPreferencesId(systemId.dbname, systemCoreConfig.coreID, port),
                    null
                )

                val currentController = controllers
                    .firstOrNull { it.name == currentName } ?: controllers.first()

                port to currentController
            }
    }

    companion object {
        fun getSharedPreferencesId(systemId: String, coreID: CoreID, port: Int) =
            "pref_key_controller_type_${systemId}_${coreID.coreName}_$port"
    }
}
