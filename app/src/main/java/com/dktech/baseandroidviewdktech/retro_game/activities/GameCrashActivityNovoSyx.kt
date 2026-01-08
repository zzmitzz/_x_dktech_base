package com.emulator.retro.console.game.retro_game.activities

import android.app.Activity
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.emulator.retro.console.game.bases.BaseActivityNovoSyx
import com.emulator.retro.console.game.databinding.ActivityCrashBinding
import kotlin.jvm.java

class GameCrashActivityNovoSyx : BaseActivityNovoSyx<ActivityCrashBinding>(ActivityCrashBinding::inflate) {
    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun initData() {

    }

    override fun initView() {
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val messageDetail = intent.getStringExtra(EXTRA_MESSAGE_DETAIL)

        binding.text1.isVisible = !message.isNullOrEmpty()
        binding.text1.text = message

        binding.text2.isVisible = !messageDetail.isNullOrEmpty()
        binding.text2.text = messageDetail
    }

    override fun initActionView() {

    }

    companion object {
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val EXTRA_MESSAGE_DETAIL = "EXTRA_MESSAGE_DETAIL"

        fun launch(activity: Activity, message: String, messageDetail: String?) {
            val intent = Intent(activity, GameCrashActivityNovoSyx::class.java).apply {
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_MESSAGE_DETAIL, messageDetail)
            }
            activity.startActivity(intent)
        }
    }
}
