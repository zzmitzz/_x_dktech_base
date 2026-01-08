package com.emulator.retro.console.game.retro_game.dialogs

import com.emulator.retro.console.game.bases.BaseDialogFragmentNovoSyx
import com.emulator.retro.console.game.databinding.DialogOverwriteConfigBinding
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class OverwriteDialogNovoSyx: BaseDialogFragmentNovoSyx<DialogOverwriteConfigBinding>(DialogOverwriteConfigBinding::inflate) {

    private var iSlot: Int = -1
    private var onDone: (Int) -> Unit = { _ -> }

    override fun initView() {

    }

    override fun initActionView() {
        binding.btnCancel.setOnUnDoubleClick { dismiss() }

        binding.btnDone.setOnUnDoubleClick {
            dismiss()
            onDone(iSlot)
        }
    }

    class Builder(iSlot: Int) {
        private val mDialog = OverwriteDialogNovoSyx()

        init {
            mDialog.iSlot = iSlot
        }

        fun addOnDone(onDone: (Int) -> Unit): Builder {
            mDialog.onDone = onDone
            return this
        }

        fun build(): OverwriteDialogNovoSyx {
            mDialog.isInit = true
            return mDialog
        }
    }
}