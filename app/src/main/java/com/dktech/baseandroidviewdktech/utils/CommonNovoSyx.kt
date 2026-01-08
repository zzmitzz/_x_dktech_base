package com.dktech.baseandroidviewdktech.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.ads.detech.AdmobUtils
import com.ads.detech.AdmobUtils.adImpressionFacebookSDK
import com.ads.detech.AppOpenManager
import com.ads.detech.utils.admod.callback.RewardAdCallback
import com.example.ratingdialog.RatingDialog
import com.emulator.retro.console.game.BuildConfig
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.models.LanguageModelNovoSyx
import com.google.android.gms.ads.AdValue
import kotlin.math.roundToInt

object CommonNovoSyx {
    private const val IS_FIRST_OPEN = "app.is_first_open"
    var isFirst: Boolean
        get() {
            return SharePrefManagerNovoSyx.get(IS_FIRST_OPEN, true)
        }
        set(value) {
            SharePrefManagerNovoSyx.put(IS_FIRST_OPEN, value)
        }

    var showRate = false
    var idRewardDownload = ""
    var keySolar = ""
    var countDownload = 0

    var isFailNativeFullScreen = false
    var isFailNativeFullScreen2 = false

    object AppLanguage {
        private const val LANGUAGE_SELECTED = "language.selected"
        private val defaultLangSelected by lazy { LanguageModelNovoSyx(R.drawable.english, R.string.english, "en") }
        var languageSelected: LanguageModelNovoSyx
            set(value) {
                SharePrefManagerNovoSyx.put(LANGUAGE_SELECTED, value)
            }
            get() = SharePrefManagerNovoSyx.get(LANGUAGE_SELECTED, defaultLangSelected)

        val appLanguages by lazy {
            listOf(
                LanguageModelNovoSyx(R.drawable.english, R.string.english, "en"),
                LanguageModelNovoSyx(R.drawable.hindi, R.string.hindi, "hi"),
                LanguageModelNovoSyx(R.drawable.spanish, R.string.spanish, "es"),
                LanguageModelNovoSyx(R.drawable.french, R.string.french, "fr"),
                LanguageModelNovoSyx(R.drawable.arabic, R.string.arabic, "ar"),
                LanguageModelNovoSyx(R.drawable.bengali, R.string.bengali, "bn"),
                LanguageModelNovoSyx(R.drawable.russian, R.string.russian, "ru"),
                LanguageModelNovoSyx(R.drawable.portuguese, R.string.portuguese, "pt"),
                LanguageModelNovoSyx(R.drawable.indonesian, R.string.indonesian, "in"),
                LanguageModelNovoSyx(R.drawable.german, R.string.german, "de"),
                LanguageModelNovoSyx(R.drawable.italian, R.string.italian, "it"),
                LanguageModelNovoSyx(R.drawable.korean, R.string.korean, "ko")
            )
        }
    }

    object Theme {
        private const val THEME_SELECTED_INDEX = "game.theme.iSelected"
        var iTheme: Int
            get() = SharePrefManagerNovoSyx.get(THEME_SELECTED_INDEX, -1)
            set(value) = SharePrefManagerNovoSyx.put(THEME_SELECTED_INDEX, value)
    }

    private val hitRect = Rect()
    private val location = IntArray(2)
    fun checkIsInside(ev: MotionEvent, view: View): Boolean {
        if (!view.isLaidOut) return false

        view.getLocationInWindow(location)
        view.getHitRect(hitRect)
        val x = location[0]
        val y = location[1]
        hitRect.offsetTo(x, y)
        return hitRect.contains(ev.x.roundToInt(), ev.y.roundToInt())
    }

    fun showPolicy(mContext: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, ConstantsNovoSyx.PRIVACY_POLICY_URI.toUri())
            mContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    fun feedbackApp(mContext: Context) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(mContext.getString(R.string.email_rating)))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Feedback ${mContext.getString(R.string.app_name)} - ${BuildConfig.VERSION_NAME}"
                )
                putExtra(Intent.EXTRA_TEXT, "")
            }
            mContext.startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (_: ActivityNotFoundException) {
        }
    }

    fun shareApp(mContext: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name))
            val shareMessage =
                "https://play.google.com/store/apps/details?id=${mContext.packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            mContext.startActivity(Intent.createChooser(shareIntent, "Choose app to share"))
        } catch (_: ActivityNotFoundException) {
        }
    }

    @JvmStatic
    fun showDialogRate(context: Activity) {
        try {
            if (context.isFinishing || context.isDestroyed) {
                return
            }
            if (showRate) {
                return
            }
            showRate = true

            val ratingDialog1 = RatingDialog.Builder(context)
                .session(1).date(1)
                .setNameApp(context.getString(R.string.app_name))
                .setIcon(R.drawable.logo_round)
                .setEmail("novosyxco@novosyx.com")
                .isShowButtonLater(true)
                .isClickLaterDismiss(true)
                .setTextButtonLater("Maybe Later")
                .setOnlickMaybeLate {
                }
                .setOnlickRate {
                }
                .ratingButtonColor(R.drawable.bg_bottom_home)
                .ignoreRated(false)
                .build()

            ratingDialog1.setCanceledOnTouchOutside(false)
            ratingDialog1.show()

        } catch (_: Exception) {
        }
    }

    fun setCountDown(context: Context) {
        val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        preferences.edit().putInt("KEY_COUNT_DOWNLOAD", getCountDown(context) + 1).apply()
    }

    fun getCountDown(context: Context): Int {
        val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return preferences.getInt("KEY_COUNT_DOWNLOAD", 0)
    }

    fun showRewardAds(context: Activity, closeAds : () -> Unit) {
        if (!AdmobUtils.isNetworkConnected(context)) {
            Toast.makeText(context,
                context.getString(R.string.the_advertisement_isn_t_ready_yet_please_try_again_in_a_few_seconds_or_check_your_connection),
                Toast.LENGTH_SHORT).show()
            return
        }

        AdmobUtils.loadAndShowAdRewardWithCallback(context, idRewardDownload, object : RewardAdCallback {
            override fun onAdClosed() {
                closeAds.invoke()
            }

            override fun onAdShowed() {
                AppOpenManager.getInstance().isAppResumeEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        AdmobUtils.dismissAdDialog()
                    } catch (_: Exception) {

                    }
                }, 800)
            }

            override fun onAdFail(p0: String?) {
                Log.d("CheckFail====", "onAdFail: $p0")
                Toast.makeText(context,
                    context.getString(R.string.the_advertisement_isn_t_ready_yet_please_try_again_in_a_few_seconds_or_check_your_connection),
                    Toast.LENGTH_SHORT).show()
            }

            override fun onEarned() {

            }

            override fun onPaid(p0: AdValue?, p1: String?) {
                p0?.let { adImpressionFacebookSDK(context, it) }
            }
        }, enableLoadingDialog = true)
    }
}