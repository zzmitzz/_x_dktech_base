package com.emulator.retro.console.game.retro_game.menu

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.databinding.LayoutGameMenuBinding
import com.emulator.retro.console.game.retro_game.activities.GameActivityNovoSyx
import com.emulator.retro.console.game.retro_game.activities.LoadStateActivityNovoSyx
import com.emulator.retro.console.game.retro_game.activities.SaveStateActivityNovoSyx
import com.emulator.retro.console.game.retro_game.activities.SettingsActivityNovoSyx
import com.emulator.retro.console.game.retro_game.pauseMenuAction.EmulairCoreOptionNovoSyx
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class GameMenu(
    private val mActivity: GameActivityNovoSyx,
    private val getInfosToCoreSettings: () -> Triple<List<EmulairCoreOptionNovoSyx>, List<EmulairCoreOptionNovoSyx>, Game>,
    private val getInfosToLoadOrSave: () -> Pair<Game, SystemCoreConfig>,
    private val onShowed: () -> Unit,
    private val onHided: () -> Unit
) {
    companion object {
        private const val SAVE_STATE_ACTION = "game.menu.action.save"
        private const val SLOT_INDEX = "game.menu.save_state.slot_index"
        private const val STATE_NAME = "game.menu.save_state.name"
        private const val STATE_TIME = "game.menu.save_state.time"

        private const val LOAD_STATE_ACTION = "game.menu.action.load"
        private const val LOAD_SLOT_INDEX = "game.menu.load_state.slot_index"

        fun createIntentSaveState(iSlot: Int, name: String, time: Long): Intent {
            return Intent().apply {
                action = SAVE_STATE_ACTION
                putExtra(SLOT_INDEX, iSlot)
                putExtra(STATE_NAME, name)
                putExtra(STATE_TIME, time)
            }
        }

        fun createIntentLoadState(iSlot: Int): Intent {
            return Intent().apply {
                action = LOAD_STATE_ACTION
                putExtra(LOAD_SLOT_INDEX, iSlot)
            }
        }
    }

    private val mLauncher = mActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val mIntent = it.data
            when(mIntent?.action) {
                SAVE_STATE_ACTION -> {
                    val iSlot = mIntent.getIntExtra(SLOT_INDEX, -1)
                    val name = mIntent.getStringExtra(STATE_NAME)
                        .takeIf { n -> !n.isNullOrBlank() } ?: "State $iSlot"
                    val time = mIntent.getLongExtra(STATE_TIME, System.currentTimeMillis())
                    if (iSlot != -1) {
                        hide()
                        mActivity.handleAction(SaveStateAction(iSlot, name, time))
                    } else {
                        Toast.makeText(
                            mActivity,
                            R.string.has_error_while_saving,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                LOAD_STATE_ACTION -> {
                    val iSlot = mIntent.getIntExtra(LOAD_SLOT_INDEX, -1)
                    if (iSlot != -1) {
                        hide()
                        mActivity.handleAction(LoadStateAction(iSlot))
                    } else {
                        Toast.makeText(
                            mActivity,
                            R.string.has_error_while_loading,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private val binding: LayoutGameMenuBinding by lazy { mActivity.binding.lMenu }
    private val mDWidth by lazy { binding.root.context.resources.displayMetrics.widthPixels.toFloat() }
    private val mAnimDuration = 100L

    val isShowed: Boolean
        get() = binding.root.isVisible
    private var isHiding: Boolean = false

    private var mAnim: ViewPropertyAnimator? = null

    fun initView() {


        binding.scv.post {
            binding.scv.translationX = -mDWidth
            binding.root.visibility = View.GONE
        }
    }

    fun initActionView() {
        binding.tvResume.setOnUnDoubleClick {
            hide()
        }

        binding.tvSave.setOnUnDoubleClick {
            navToSaveState()
        }

        binding.tvLoad.setOnUnDoubleClick {
            navToLoadState()
        }

        binding.tvCheatCode.setOnUnDoubleClick {
            navToCheatCode()
        }

        binding.tvQuit.setOnUnDoubleClick {
            // TODO: 10/9/2025 dialog confirm?
            if (mActivity.handleAction(QuitAction)) hide()
        }

        binding.tvRestart.setOnUnDoubleClick {
            // TODO: 10/9/2025 dialog confirm?
            if (mActivity.handleAction(RestartAction)) hide()
        }

        binding.tvMute.setOnUnDoubleClick {
            if (!binding.tvMute.isEnabled) {
                Toast.makeText(
                    mActivity,
                    R.string.no_gaming_device_found,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnUnDoubleClick
            }

            val isMute = !binding.tvMute.isChecked
            if (mActivity.handleAction(MuteAction(isMute))) {
                binding.tvMute.isChecked = isMute
            }
        }

        binding.tvFast.setOnUnDoubleClick {
            if (!binding.tvFast.isEnabled) {
                Toast.makeText(
                    mActivity,
                    R.string.fast_forward_not_supported,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnUnDoubleClick
            }

            val isFast = !binding.tvFast.isChecked
            if (mActivity.handleAction(ForwardAction(isFast))) {
                binding.tvFast.isChecked = isFast
            }
        }

        binding.tvSetting.setOnUnDoubleClick {
            navToSetting()
        }
    }

    private fun navToCheatCode() {
        // TODO: 10/9/2025
    }

    private fun navToLoadState() {
        val (game, systemCoreConfig) = getInfosToLoadOrSave()
        LoadStateActivityNovoSyx.createIntent(mActivity, game, systemCoreConfig).apply {
            mLauncher.launch(this)
        }
    }

    private fun navToSaveState() {
        val (game, systemCoreConfig) = getInfosToLoadOrSave()
        SaveStateActivityNovoSyx.createIntent(mActivity, game, systemCoreConfig).apply {
            mLauncher.launch(this)
        }
    }

    private fun navToSetting() {
        val (mCoresOption, mAdvancedCoresOption, mGame) = getInfosToCoreSettings()
        SettingsActivityNovoSyx.createIntent(mActivity, mCoresOption, mAdvancedCoresOption, mGame).apply {
            if (this == null) {
                Toast.makeText(
                    mActivity,
                    R.string.there_was_an_error_retrieving_some_information,
                    Toast.LENGTH_SHORT
                ).show()
            }   else {
                mLauncher.launch(this)
            }
        }
    }

    fun setupBeforeShow(
        isMute: Boolean?,
        isFastForwardSupport: Boolean,
        isFastForward: Boolean
    ) {
        binding.tvMute.isChecked = isMute ?: false
        binding.tvMute.isEnabled = isMute != null

        binding.tvFast.isEnabled = isFastForwardSupport
        binding.tvFast.isChecked = isFastForward
    }

    fun show() {
        if (isShowed) return

        mAnim?.cancel()
        mAnim = null

        binding.root.visibility = View.VISIBLE
        mAnim = binding.root.animate()
            .alpha(1f)
            .setDuration(mAnimDuration)
            .setInterpolator { inter ->
                binding.scv.translationX = -(1 - inter) * mDWidth
                inter
            }
            .withEndAction {
                mAnim = null
                binding.scv.translationX = 0f
                onShowed()
            }
        mAnim?.start()
    }

    fun hide() {
        if (!isShowed || isHiding) return

        mAnim?.cancel()
        mAnim = null

        isHiding = true
        mAnim = binding.root.animate()
            .alpha(0f)
            .setDuration(mAnimDuration)
            .withEndAction {
                binding.scv.translationX = -mDWidth
                binding.root.visibility = View.GONE
                isHiding = false
                onHided()
            }
        mAnim?.start()
    }
}

interface IGameMenuAction
object QuitAction: IGameMenuAction
object RestartAction: IGameMenuAction
data class MuteAction(val isEnable: Boolean): IGameMenuAction
data class ForwardAction(val isEnable: Boolean): IGameMenuAction
data class SaveStateAction(val iSlot: Int, val name: String, val time: Long): IGameMenuAction
data class LoadStateAction(val iSlot: Int): IGameMenuAction