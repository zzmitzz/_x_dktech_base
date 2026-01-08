package com.emulator.retro.console.game.retro_game.pauseMenuAction

import com.bigbratan.emulair.common.managers.coresLibrary.CoreVariable
import com.swordfish.libretrodroid.Variable
import java.io.Serializable

data class CoreOptionNovoSyx(
    val variable: CoreVariable,
    val name: String,
    val optionValues: List<String>
) : Serializable {

    companion object {
        fun fromLibretroDroidVariable(variable: Variable): CoreOptionNovoSyx {
            val name = variable.description?.split(";")?.get(0)!!
            val values = variable.description?.split(";")?.get(1)?.trim()?.split('|') ?: listOf()
            val coreVariable = CoreVariable(variable.key!!, variable.value!!)
            return CoreOptionNovoSyx(coreVariable, name, values)
        }
    }
}
