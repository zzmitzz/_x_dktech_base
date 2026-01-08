package com.emulator.retro.console.game.retro_game.manager.rumble

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.InputDevice
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.utils.coroutines.safeCollect
import com.emulator.retro.console.game.retro_game.manager.input.InputDeviceManagerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.settings.SettingsManagerNovoSyx
import com.swordfish.libretrodroid.RumbleEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.newSingleThreadContext
import kotlin.math.roundToInt

class RumbleManagerNovoSyx(
    applicationContext: Context,
    private val settingsManager: SettingsManagerNovoSyx,
    private val inputDeviceManager: InputDeviceManagerNovoSyx
) {
    private val deviceVibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val rumbleContext = newSingleThreadContext("Rumble")

    suspend fun collectAndProcessRumbleEvents(
        systemCoreConfig: SystemCoreConfig,
        rumbleEventsObservable: Flow<RumbleEvent>
    ) {
        val enableRumble = settingsManager.enableRumble()
        val rumbleSupported = systemCoreConfig.rumbleSupported

        if (!enableRumble && rumbleSupported) {
            return
        }

        inputDeviceManager.getEnabledInputsObservable()
            .map { getVibrators(it) }
            .flatMapLatest { vibrators ->
                rumbleEventsObservable
                    .onEach { runCatching { vibrate(vibrators[it.port], it) } }
                    .onStart { stopAllVibrators(vibrators) }
                    .onCompletion { stopAllVibrators(vibrators) }
                    .flowOn(rumbleContext)
            }
            .safeCollect { }
    }

    private fun stopAllVibrators(vibrators: List<Vibrator>) {
        vibrators.forEach {
            runCatching { it.cancel() }
        }
    }

    private suspend fun getVibrators(gamePads: List<InputDevice>): List<Vibrator> {
        val enableDeviceRumble = settingsManager.enableDeviceRumble()

        return if (gamePads.isEmpty() && enableDeviceRumble) {
            listOf(deviceVibrator)
        } else {
            gamePads.map { it.vibrator }
        }
    }

    private fun vibrate(vibrator: Vibrator?, rumbleEvent: RumbleEvent) {
        if (vibrator == null) return

        vibrator.cancel()

        val amplitude = computeAmplitude(rumbleEvent)

        if (amplitude == 0) return

        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(MAX_RUMBLE_DURATION_MS, amplitude))
        } else if (amplitude > LEGACY_MIN_RUMBLE_STRENGTH) {
            vibrator.vibrate(MAX_RUMBLE_DURATION_MS)
        }
    }

    private fun computeAmplitude(rumbleEvent: RumbleEvent): Int {
        val strength = rumbleEvent.strengthStrong * 0.66f + rumbleEvent.strengthWeak * 0.33f
        return (DEFAULT_RUMBLE_STRENGTH * (strength) * 255).roundToInt()
    }

    companion object {
        const val MAX_RUMBLE_DURATION_MS = 1000L
        const val DEFAULT_RUMBLE_STRENGTH = 0.5f
        const val LEGACY_MIN_RUMBLE_STRENGTH = 100
    }
}
