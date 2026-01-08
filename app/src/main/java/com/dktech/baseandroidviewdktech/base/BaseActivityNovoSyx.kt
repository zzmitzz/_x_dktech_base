package com.dktech.baseandroidviewdktech.base

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.emulator.retro.console.game.dialogs.LoadingDialogNovoSyx
import com.emulator.retro.console.game.dialogs.NoInternetDialogNovoSyx
import com.emulator.retro.console.game.receivers.InternetStatusReceiverNovoSyx
import com.emulator.retro.console.game.utils.CommonNovoSyx
import com.emulator.retro.console.game.utils.setFullScreenVisibility
import java.util.Locale

abstract class BaseActivityNovoSyx<viewBinding : ViewBinding>(val inflater: (LayoutInflater) -> viewBinding) :
    AppCompatActivity() {

    protected abstract val onBackPressedCallback: OnBackPressedCallback

    protected open val lAddInsert: ViewGroup? = null

    private val noNetworkDialog by lazy { NoInternetDialogNovoSyx(this) }
    open val isRegisterNetworkReceiver: Boolean = true
    private var internetStatusReceiver: InternetStatusReceiverNovoSyx? = null

    val binding: viewBinding by lazy { inflater(layoutInflater) }
    private val loadingDialog by lazy { LoadingDialogNovoSyx(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFullScreenVisibility()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        if (isFinishNow()) return

        lAddInsert?.let { mLayout ->
            val oldTop = mLayout.paddingTop
            ViewCompat.setOnApplyWindowInsetsListener(mLayout) { _, insets ->
                mLayout.setPadding(
                    mLayout.paddingLeft,
                    oldTop + insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top,
                    mLayout.paddingRight,
                    mLayout.paddingBottom
                )
                insets
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        initData()
        initView()
        initActionView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.setFullScreenVisibility()
    }

    protected open fun isFinishNow(): Boolean = false

    abstract fun initData()

    abstract fun initView()

    abstract fun initActionView()

    override fun onResume() {
        super.onResume()
        if (isRegisterNetworkReceiver) {
            internetStatusReceiver = InternetStatusReceiverNovoSyx {
                if (!isFinishing && !isDestroyed) {
                    onNetworkStatusChange(it)

                    if (it && noNetworkDialog.isShowing) {
                        noNetworkDialog.dismiss()
                    } else if (!it && !noNetworkDialog.isShowing) {
                        noNetworkDialog.show()
                    }
                }

            }
            registerReceiver(internetStatusReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRegisterNetworkReceiver) {
            val mReceiver = internetStatusReceiver
            internetStatusReceiver = null
            if (mReceiver != null) {
                try { unregisterReceiver(mReceiver) } finally { }
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let { mBase ->
            val locale = Locale(CommonNovoSyx.AppLanguage.languageSelected.key)
            Locale.setDefault(locale)

            val configuration = mBase.resources.configuration
            configuration.setLocale(locale)
            mBase.createConfigurationContext(configuration)
        })
    }

    fun showLoading(isShow: Boolean) {
        if (!isShow && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        } else if (isShow && !loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }

    open fun onNetworkStatusChange(hasNetwork: Boolean) {}

}