package com.dktech.baseandroidviewdktech.ui.setting

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivitySettingBinding
import com.dktech.baseandroidviewdktech.ui.language_screen.LanguageScreenActivity
import com.dktech.baseandroidviewdktech.utils.helper.getSelectedLanguage
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    override fun getViewBinding(): ActivitySettingBinding = ActivitySettingBinding.inflate(layoutInflater)

    override fun initData() {
    }

    override fun initView() {
        runBlocking {
            val language = getSelectedLanguage(this@SettingActivity)
            withContext(Dispatchers.Main) {
                binding.tvLanguage.text = getString(language.name)
            }
        }
    }

    override fun initEvent() {
        binding.imageView.setSafeOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
        binding.btnLanguage.setSafeOnClickListener {
            Intent(this, LanguageScreenActivity::class.java).apply {
                putExtra("fromHome", true)
                startActivity(this)
            }
        }
    }

    override fun initObserver() {
    }
}
