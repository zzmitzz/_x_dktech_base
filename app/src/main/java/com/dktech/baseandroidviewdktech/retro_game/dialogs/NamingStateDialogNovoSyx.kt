package com.emulator.retro.console.game.retro_game.dialogs

import android.widget.Toast
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.bases.BaseDialogFragmentNovoSyx
import com.emulator.retro.console.game.databinding.DialogNamingStateBinding
import com.emulator.retro.console.game.utils.formatDateTime
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class NamingStateDialogNovoSyx: BaseDialogFragmentNovoSyx<DialogNamingStateBinding>(
    DialogNamingStateBinding::inflate
) {

    private val mTime = System.currentTimeMillis()
    private var iSlot: Int = -1
    private var onDone: (Int, String, Long) -> Unit = { _, _, _ -> }

    override fun initView() {
        binding.tvDateTime.text = mTime.formatDateTime("dd/MM/yyyy, hh:mm")
    }

    override fun initActionView() {
        binding.btnCancel.setOnUnDoubleClick { dismiss() }
        binding.ivClose.setOnUnDoubleClick { dismiss() }

        binding.btnDone.setOnUnDoubleClick {
            val name = binding.edtName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.enter_name_state,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnUnDoubleClick
            }

            dismiss()
            onDone(iSlot, name, mTime)
        }
    }

    class Builder(iSlot: Int) {
        private val mDialog = NamingStateDialogNovoSyx()

        init {
            mDialog.iSlot = iSlot
        }

        fun addOnDone(onDone: (Int, String, Long) -> Unit): Builder {
            mDialog.onDone = onDone
            return this
        }

        fun build(): NamingStateDialogNovoSyx {
            mDialog.isInit = true
            return mDialog
        }
    }
}