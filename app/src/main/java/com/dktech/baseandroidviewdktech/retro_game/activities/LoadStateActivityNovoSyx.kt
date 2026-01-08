package com.emulator.retro.console.game.retro_game.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.bumptech.glide.Glide
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.bases.BaseActivityNovoSyx
import com.emulator.retro.console.game.databinding.ActivityLoadStateBinding
import com.emulator.retro.console.game.retro_game.GameLibraryInstanceNovoSyx
import com.emulator.retro.console.game.retro_game.adapters.LoadStateAdapterNovoSyx
import com.emulator.retro.console.game.retro_game.menu.GameMenu
import com.emulator.retro.console.game.utils.setOnUnDoubleClick
import kotlinx.coroutines.launch

class LoadStateActivityNovoSyx :
    BaseActivityNovoSyx<ActivityLoadStateBinding>(ActivityLoadStateBinding::inflate) {
    companion object {
        private const val GAME = "activity.load_state.intent.game"
        private const val SYS_CORE_CFG = "activity.load_state.intent.sys_core_cfg"

        fun createIntent(
            mContext: Context,
            game: Game,
            systemCoreConfig: SystemCoreConfig
        ): Intent {
            return Intent(mContext, LoadStateActivityNovoSyx::class.java).apply {
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

    override val lAddInsert: ViewGroup by lazy { binding.lMainContainer }

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

    private val mAdapter by lazy {
        LoadStateAdapterNovoSyx(Glide.with(this), ::setResultAndFinish)
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

            val mStates = mInstances.mStatesHelper.getLoadStates(mGame, mCore.coreID)

            showLoading(false)
            mAdapter.submitList(mStates)
            binding.lEmpty.isVisible = mStates.isEmpty()
        }
    }

    override fun initActionView() {
        binding.btnBack.setOnUnDoubleClick { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setResultAndFinish(iSlot: Int) {
        setResult(RESULT_OK, GameMenu.createIntentLoadState(iSlot))
        finish()
    }
}