package com.emulator.retro.console.game.retro_game.pauseMenuAction

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.bigbratan.emulair.common.metadata.retrograde.ExposedSetting
import java.io.Serializable

data class EmulairCoreOptionNovoSyx(
    private val exposedSetting: ExposedSetting,
    private val coreOption: CoreOptionNovoSyx
) : Serializable {

    fun getKey(): String {
        return exposedSetting.key
    }

    fun getDisplayName(context: Context): String {
        return context.getString(exposedSetting.titleId)
    }

    fun getEntries(context: Context): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues
        }

        return getCorrectExposedSettings().map { context.getString(it.titleId) }
    }

    fun getEntriesValues(): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues.map { it }
        }

        return getCorrectExposedSettings().map { it.key }
    }

    fun getCurrentValue(): String {
        return coreOption.variable.value
    }

    fun getCurrentIndex(): Int {
        return maxOf(getEntriesValues().indexOf(getCurrentValue()), 0)
    }

    private fun getCorrectExposedSettings(): List<ExposedSetting.Value> {
        return exposedSetting.values
            .filter { it.key in coreOption.optionValues }
    }

    // TODO: 10/9/2025 new
    fun getDisplayName(): Int = exposedSetting.titleId

    fun getCurrentValue(mContext: Context): String {
        return exposedSetting.values.firstOrNull {
            it.key == coreOption.variable.value
        }?.titleId?.let(mContext::getString) ?: coreOption.variable.value
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<EmulairCoreOptionNovoSyx>() {
            override fun areItemsTheSame(
                oldItem: EmulairCoreOptionNovoSyx,
                newItem: EmulairCoreOptionNovoSyx
            ): Boolean {
                return oldItem.coreOption.name == oldItem.coreOption.name && newItem.exposedSetting.key == newItem.exposedSetting.key
            }

            override fun areContentsTheSame(
                oldItem: EmulairCoreOptionNovoSyx,
                newItem: EmulairCoreOptionNovoSyx
            ): Boolean {
                return true
            }
        }
    }
}
