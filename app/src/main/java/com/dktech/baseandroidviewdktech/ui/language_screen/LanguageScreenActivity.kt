package com.dktech.baseandroidviewdktech.ui.language_screen

import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.ui_models.LanguageModel
import com.dktech.baseandroidviewdktech.base.ui_models.getLanguageList
import com.dktech.baseandroidviewdktech.databinding.LayoutActivityLanguageBinding
import com.dktech.baseandroidviewdktech.utils.helper.getSelectedLanguage
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import com.dktech.baseandroidviewdktech.utils.helper.setSelectedLanguage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class LanguageScreenActivity :
    BaseActivity<LayoutActivityLanguageBinding>() {
    private val scope = lifecycleScope + CoroutineExceptionHandler { e, t ->
        Log.d(TAG, "initView: $e")
    }
    private val languageAdapter by lazy {
        LanguageAdapter(
            context = this,
            languageList = getLanguageList(),
            onFirstSelect = {
                // save selected language
            }
        ).apply {
            scope.launch {
                setLanguage(getSelectedLanguage(this@LanguageScreenActivity))
            }
        }
    }
    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nextIntroActivity()
            }
        }

    override fun getViewBinding(): LayoutActivityLanguageBinding {
        return LayoutActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun initData() {
    }

    override fun initView() {
        binding.rcvLanguage.apply {
            adapter = languageAdapter
            setHasFixedSize(true)
            layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@LanguageScreenActivity)
        }
    }

    override fun initEvent() {
        binding.ivDone.setSafeOnClickListener {
            val selectedLanguage = languageAdapter.getSelectedPositionLanguage()
            if (selectedLanguage == null) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_language_first),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                scope.launch {
                    setLanguage(selectedLanguage)
                }
            }
        }
    }


    override fun initObserver() {

    }

    private fun setLanguage(language: LanguageModel) {
        scope.launch {
            setSelectedLanguage(this@LanguageScreenActivity, language)
        }
    }

    private fun nextIntroActivity() {

    }


}