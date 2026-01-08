package com.emulator.retro.console.game.retro_game.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.bumptech.glide.Glide
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.bases.BaseActivityNovoSyx
import com.emulator.retro.console.game.databinding.ActivitySaveStateBinding
import com.emulator.retro.console.game.retro_game.GameLibraryInstanceNovoSyx
import com.emulator.retro.console.game.retro_game.adapters.SaveStateAdapterNovoSyx
import com.emulator.retro.console.game.retro_game.dialogs.NamingStateDialogNovoSyx
import com.emulator.retro.console.game.retro_game.dialogs.OverwriteDialogNovoSyx
import com.emulator.retro.console.game.retro_game.menu.GameMenu
import com.emulator.retro.console.game.utils.setOnUnDoubleClick
import kotlinx.coroutines.launch

class SaveStateActivityNovoSyx: BaseActivityNovoSyx<ActivitySaveStateBinding>(
    ActivitySaveStateBinding::inflate
) {
    companion object {
        private const val GAME = "activity.save_state.intent.game"
        private const val SYS_CORE_CFG = "activity.save_state.intent.sys_core_cfg"

        fun createIntent(mContext: Context, game: Game, systemCoreConfig: SystemCoreConfig): Intent {
            return Intent(mContext, SaveStateActivityNovoSyx::class.java).apply {
                putExtra(GAME, game)
                putExtra(SYS_CORE_CFG, systemCoreConfig)
            }
        }
    }

    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }


    private val mInstances by lazy { GameLibraryInstanceNovoSyx(application) }

    private val game: Game? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(GAME, Game::class.java)
        } else {
            intent.getSerializableExtra(GAME) as? Game
        }
    }

    private val systemCoreConfig: SystemCoreConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(SYS_CORE_CFG, SystemCoreConfig::class.java)
        } else {
            intent.getSerializableExtra(SYS_CORE_CFG) as? SystemCoreConfig
        }
    }

    private val mAdapter by lazy {
        SaveStateAdapterNovoSyx(Glide.with(this)) { index, isReplace ->
            if (isReplace) {
                OverwriteDialogNovoSyx.Builder(index)
                    .addOnDone(::showNamingDialog)
                    .build()
                    .show(supportFragmentManager, OverwriteDialogNovoSyx::class.simpleName)
            } else showNamingDialog(index)
        }
    }

    override fun isFinishNow(): Boolean {
        val isF = game == null || systemCoreConfig == null
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
        binding.rcvItems.adapter = mAdapter
        binding.rcvItems.layoutManager = GridLayoutManager(this, 3)
        lifecycleScope.launch {
            showLoading(true)
            val mGame = game ?: return@launch
            val mCore = systemCoreConfig ?: return@launch

            val mStates = mInstances.mStatesHelper.getSaveStates(mGame, mCore.coreID)

            showLoading(false)
            mAdapter.submitList(mStates)
        }
    }

    override fun initActionView() {
        binding.btnBack.setOnUnDoubleClick { onBackPressedDispatcher.onBackPressed() }

    }

    private fun showNamingDialog(index: Int) {
        NamingStateDialogNovoSyx.Builder(index)
            .addOnDone(::setResultAndFinish)
            .build()
            .show(supportFragmentManager, NamingStateDialogNovoSyx::class.simpleName)
    }

    private fun setResultAndFinish(iSlot: Int, name: String, time: Long) {
        setResult(RESULT_OK, GameMenu.createIntentSaveState(iSlot, name, time))
        finish()
    }
}