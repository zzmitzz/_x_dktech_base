package com.dktech.baseandroidviewdktech.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.dktech.baseandroidviewdktech.base.dialog.InternetErrorDialog
import com.dktech.baseandroidviewdktech.ui.splash_screen.SplashScreenActivity
import com.dktech.baseandroidviewdktech.utils.helper.NetworkUtils
import com.dktech.baseandroidviewdktech.utils.helper.getSelectedLanguage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale


@Suppress("DEPRECATION")
abstract class BaseActivity<viewBinding : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: viewBinding
    protected abstract val onBackPressedCallback: OnBackPressedCallback

    private val internetErrorDialog by lazy {
        InternetErrorDialog(
            context = this,
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        if(this::class.java.name != SplashScreenActivity::class.java.name) {
            runBlocking {
                getSelectedLanguage(this@BaseActivity).let {
                    val locale = Locale(it.key)
                    Locale.setDefault(locale)
                    val config = Configuration(resources.configuration)
                    config.setLocale(locale)
                    config.setLayoutDirection(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                }
            }
        }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        binding = getViewBinding()
        window.statusBarColor = Color.BLACK
        setContentView(binding.root)
        hideSystemUI()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        initData()
        initView()
        initEvent()
        hideNavigationBar()
        hideSystemUI()

    }

    abstract fun getViewBinding(): viewBinding
    abstract fun initData()
    abstract fun initView()
    abstract fun initEvent()
    abstract fun initObserver()


    open fun onNetworkRetry() {
        // Default implementation - override in subclasses if needed
    }

    open fun onNetworkSettings() {
        // Default implementation - override in subclasses if needed
    }

    open fun shouldShowInternetDialog(): Boolean = true

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }
    private fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            hideSystemUIBeloR()
        }
    }

    private fun hideSystemUIBeloR() {
        val decorView: View = window.decorView
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = newUiOptions
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setupNetworkMonitoring() {
        if (!shouldShowInternetDialog()) return

        lifecycleScope.launch {
            NetworkUtils.observeNetworkState(this@BaseActivity).collectLatest { isConnected ->
            }
        }
    }

    private fun checkNetworkStatus() {
        if (!shouldShowInternetDialog()) return

        if (!NetworkUtils.isNetworkConnected(this)) {
            showInternetDialog()
        }
    }

    private fun showInternetDialog() {
    }

    companion object {
        val TAG = this::class.java.simpleName
    }

}