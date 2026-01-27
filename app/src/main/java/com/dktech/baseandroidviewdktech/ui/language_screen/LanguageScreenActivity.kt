package com.dktech.baseandroidviewdktech.ui.language_screen

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.ui_models.LanguageModel
import com.dktech.baseandroidviewdktech.base.ui_models.getLanguageList
import com.dktech.baseandroidviewdktech.databinding.LayoutActivityLanguageBinding
import com.dktech.baseandroidviewdktech.ui.home.MainActivity
import com.dktech.baseandroidviewdktech.ui.intro.IntroScreenActivity
import com.dktech.baseandroidviewdktech.utils.helper.getSelectedLanguage
import com.dktech.baseandroidviewdktech.utils.helper.gone
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import com.dktech.baseandroidviewdktech.utils.helper.setSelectedLanguage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking

class LanguageScreenActivity : BaseActivity<LayoutActivityLanguageBinding>() {
    private val scope =
        lifecycleScope +
            CoroutineExceptionHandler { e, t ->
                Log.d(TAG, "initView: $e")
            }
    private val languageAdapter by lazy {
        LanguageAdapter(
            context = this,
            languageList = getLanguageList(),
            onFirstSelect = {
//                AdsManager.showNativePreload(
//                    this,
//                    RemoteConfig.NATIVE_LANGUAGE2_231225,
//                    binding.frNative,
//                )
            },
        ).apply {
            // Trigger update btn state
            onLanguageClick = {
                updateStateDoneBtn()
            }
        }
    }

    override fun shouldShowInternetDialog(): Boolean = false

    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    nextIntroActivity()
                }
            }
    private var startFromHome = false

    private var intentNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        }

    private fun updateStateDoneBtn() {
        binding.ivDone.isClickable = languageAdapter.getSelectedPositionLanguage() != null
        binding.ivDone.alpha =
            if (languageAdapter.getSelectedPositionLanguage() !=
                null
            ) {
                1f
            } else {
                0.2f
            }
    }

    override fun getViewBinding(): LayoutActivityLanguageBinding = LayoutActivityLanguageBinding.inflate(layoutInflater)

    override fun initData() {
        startFromHome = intent.getBooleanExtra("fromHome", false)
    }

    override fun initView() {
//        AdsManager.showNativePreload(this, RemoteConfig.NATIVE_LANGUAGE_231225, binding.frNative)
//        AdsManager.preloadNative(this@LanguageScreenActivity, RemoteConfig.NATIVE_INTRO1_231225)
//        AdsManager.preloadNative(this@LanguageScreenActivity, RemoteConfig.NATIVE_INTRO2_231225)
//        AdsManager.preloadNative(this@LanguageScreenActivity, RemoteConfig.NATIVE_INTRO3_231225)
        updateStateDoneBtn()
//        if (!Constants.isFailNativeFullScreen) {
//            AdsManager.preloadNativeFullScreen(this, RemoteConfig.NATIVE_FC_INTRO1_231225) {}
//        }
//        if (!Constants.isFailNativeFullScreen2) {
//            AdsManager.preloadNativeFullScreen(this, RemoteConfig.NATIVE_FC_INTRO_2_231225) {}
//        }

        binding.rcvLanguage.apply {
            adapter = languageAdapter
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(this@LanguageScreenActivity)
        }
        if (!startFromHome) {
            binding.imageView4.gone()
        }
        if (startFromHome) {
            runBlocking {
                getSelectedLanguage(this@LanguageScreenActivity).let {
                    languageAdapter.setSelectedPositionLanguage(it)
                }
            }
        }
        updateStateDoneBtn()
        intentNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun initEvent() {
        binding.ivDone.setSafeOnClickListener {
            val selectedLanguage = languageAdapter.getSelectedPositionLanguage()
            if (selectedLanguage == null) {
                Toast
                    .makeText(
                        this,
                        getString(R.string.please_select_language_first),
                        Toast.LENGTH_SHORT,
                    ).show()
            } else {
                if (startFromHome) {
                    setLanguage(selectedLanguage)
                } else {
                    setLanguage(selectedLanguage)
//                    AdsManager.showAdsInterstitial(
//                        this,
//                        RemoteConfig.INTER_LANGUAGE_231225,
//                        R.layout.ad_template_native_fullscreen,
//                    ) {
//                        setLanguage(selectedLanguage)
//                    }
                }
            }
        }
        binding.imageView4.setSafeOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    override fun initObserver() {
    }

    private fun setLanguage(language: LanguageModel) {
        setSelectedLanguage(this@LanguageScreenActivity, language) {
            nextIntroActivity()
        }
    }

    private fun nextIntroActivity() {
        if (!startFromHome) {
            val refresh = Intent(this@LanguageScreenActivity, IntroScreenActivity::class.java)
            startActivity(refresh)
            finish()
        } else {
            val refresh =
                Intent(this@LanguageScreenActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            startActivity(refresh)
            finish()
        }
    }
}
