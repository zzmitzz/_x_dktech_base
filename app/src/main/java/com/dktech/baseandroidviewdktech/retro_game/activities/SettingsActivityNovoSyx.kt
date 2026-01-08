package com.emulator.retro.console.game.retro_game.activities

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigbratan.emulair.common.managers.coresLibrary.CoreVariablesManager
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.bases.BaseActivityNovoSyx
import com.emulator.retro.console.game.databinding.ActivitySettingsBinding
import com.emulator.retro.console.game.retro_game.GameLibraryInstanceNovoSyx
import com.emulator.retro.console.game.retro_game.adapters.CoreOptionAdapterNovoSyx
import com.emulator.retro.console.game.retro_game.dialogs.OptionsDialogNovoSyx
import com.emulator.retro.console.game.retro_game.pauseMenuAction.EmulairCoreOptionNovoSyx
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class SettingsActivityNovoSyx : BaseActivityNovoSyx<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {
    companion object {
        private var coreOptions: List<EmulairCoreOptionNovoSyx>? = null
        private var advancedCoreOptions: List<EmulairCoreOptionNovoSyx>? = null
        private var game: Game? = null

        fun createIntent(
            mContext: Context,
            coreOptions: List<EmulairCoreOptionNovoSyx>?,
            advancedCoreOptions: List<EmulairCoreOptionNovoSyx>?,
            game: Game?
        ): Intent? {
            coreOptions ?: advancedCoreOptions ?: game ?: return null

            SettingsActivityNovoSyx.coreOptions = coreOptions
            SettingsActivityNovoSyx.advancedCoreOptions = advancedCoreOptions
            SettingsActivityNovoSyx.game = game
            return Intent(mContext, SettingsActivityNovoSyx::class.java)
        }
    }

    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }


    private val mInstances by lazy { GameLibraryInstanceNovoSyx.getInstance(application) }

    private val mCoreOptions by lazy { coreOptions!! }
    private val mAdvancedCoreOptions by lazy { advancedCoreOptions!! }
    private val mGame by lazy { game!! }

    private val coresHasChanged = mutableListOf<Pair<EmulairCoreOptionNovoSyx, Int>>()

    private val adapter by lazy {
        CoreOptionAdapterNovoSyx(
            getValueSelected = { mOption ->
                val index = coresHasChanged.firstOrNull {
                    it.first.getKey() == mOption.getKey()
                }?.second ?: -1

                if (index == -1) {
                    mOption.getCurrentValue(this)
                } else {
                    mOption.getEntries(this)[index]
                }
            },
            ::showDialog
        )
    }

    override fun isFinishNow(): Boolean {
        val isF = coreOptions == null || advancedCoreOptions == null || game == null

        if (isF) {
            Toast.makeText(
                this,
                R.string.there_was_an_error_retrieving_some_information,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        return isF
    }

    override fun initData() {

    }

    override fun initView() {
        binding.rcvItems.adapter = adapter
        binding.rcvItems.layoutManager = LinearLayoutManager(this)
        binding.rcvItems.itemAnimator = null

        addCoreOptions()
    }

    override fun initActionView() {
        binding.btnBack.setOnUnDoubleClick { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        coreOptions = null
        advancedCoreOptions = null
    }

    private fun addCoreOptions() {
        if (mCoreOptions.isEmpty() && mAdvancedCoreOptions.isEmpty()) return

        adapter.submitList(
            mCoreOptions.toMutableSet().apply { addAll(mAdvancedCoreOptions) }.toList()
        )
    }

    private fun showDialog(mOption: EmulairCoreOptionNovoSyx) {
        OptionsDialogNovoSyx.Builder(
            mOption,
            coresHasChanged.firstOrNull { it.first.getKey() == mOption.getKey() }?.second
        ).addOnChanged(::onHasCoreOptionChanged)
            .build()
            .show(supportFragmentManager, OptionsDialogNovoSyx::class.simpleName)
    }


    private fun onHasCoreOptionChanged(option: EmulairCoreOptionNovoSyx, index: Int) {
        coresHasChanged.removeIf { it.first.getKey() == option.getKey() }
        if (option.getCurrentIndex() != index) {
            coresHasChanged.add(option to index)
        }
        adapter.notifyItemChanged(mCoreOptions.indexOfFirst { it.getKey() == option.getKey() })
        mInstances.mSharedPref.edit {
            val key = CoreVariablesManager.computeSharedPreferenceKey(option.getKey(), mGame.systemId)
            putString(key, option.getEntriesValues()[index])
        }
    }
}