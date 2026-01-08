package com.emulator.retro.console.game.retro_game.dialogs

import androidx.recyclerview.widget.LinearLayoutManager
import com.emulator.retro.console.game.bases.BaseDialogFragmentNovoSyx
import com.emulator.retro.console.game.databinding.DialogSettingsCoreOptionBinding
import com.emulator.retro.console.game.retro_game.adapters.OptionDialogAdapterNovoSyx
import com.emulator.retro.console.game.retro_game.pauseMenuAction.EmulairCoreOptionNovoSyx

class OptionsDialogNovoSyx : BaseDialogFragmentNovoSyx<DialogSettingsCoreOptionBinding>(
    DialogSettingsCoreOptionBinding::inflate
) {

    private var option: EmulairCoreOptionNovoSyx? = null
    private var iSelected: Int? = null
    private var onChanged: (EmulairCoreOptionNovoSyx, Int) -> Unit = { _, _ -> }

    override fun initView() {
        binding.rcvItems.layoutManager = LinearLayoutManager(requireContext())

        option?.let {
            binding.tvTitle.setText(it.getDisplayName())

            binding.rcvItems.adapter = OptionDialogAdapterNovoSyx(
                it.getEntries(requireContext()),
                iSelected ?: it.getCurrentIndex()
            ) { index -> onChanged(it, index) }
        }
    }

    override fun initActionView() {

    }

    class Builder(option: EmulairCoreOptionNovoSyx, iSelected: Int?) {
        private val mDialog = OptionsDialogNovoSyx()

        init {
            mDialog.option = option
            mDialog.iSelected = iSelected
        }

        fun addOnChanged(onChanged: (EmulairCoreOptionNovoSyx, Int) -> Unit): Builder {
            mDialog.onChanged = onChanged
            return this
        }

        fun build(): OptionsDialogNovoSyx {
            mDialog.isInit = true
            return mDialog
        }
    }
}