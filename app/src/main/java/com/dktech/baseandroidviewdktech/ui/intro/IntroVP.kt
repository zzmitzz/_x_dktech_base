package com.dktech.baseandroidviewdktech.ui.intro

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dktech.baseandroidviewdktech.ui.intro.fragments.IntoAFragment
import com.dktech.baseandroidviewdktech.ui.intro.fragments.IntoCFragment
import com.dktech.baseandroidviewdktech.ui.intro.fragments.IntroBFragment

class IntroVP(
    val activity: AppCompatActivity,
    val onAClick: () -> Unit,
    val onBClick: () -> Unit,
    val onCClick: () -> Unit,
    val onNativeFragClick: () -> Unit = {},
) : FragmentStateAdapter(activity) {
    val listFragment =
        mutableListOf(
            IntoAFragment.newInstance(
//                checkNativeLayout(Constants.JSON_INTRO_1, 0) && !AdmobUtils.isTestDevice,
                false,
            ) {
                onAClick()
            },
            IntroBFragment.newInstance(
//                checkNativeLayout(Constants.JSON_INTRO_2, 1) && !AdmobUtils.isTestDevice,
                false,
            ) {
                onBClick()
            },
            IntoCFragment.newInstance(
//                checkNativeLayout(Constants.JSON_INTRO_3, 2) && !AdmobUtils.isTestDevice,
                true,
            ) {
                onCClick()
//                AdsManager.showAdsInterstitial(
//                    activity,
//                    RemoteConfig.INTER_INTRO3_231225,
//                    R.layout.ad_template_native_fullscreen,
//                ) {
//                    onCClick()
//                }
            },
        )

//    fun updateListFragment() {
//        if (!AdmobUtils.isTestDevice && !Constants.isFailNativeFullScreen && checkNativeIntroEnable(Constants.JSON_FULL_INTRO_1)) {
//            listFragment.add(1, NativeFragment.newInstance(1, onNativeFragClick))
//        }
//        if (!AdmobUtils.isTestDevice && !Constants.isFailNativeFullScreen2 && checkNativeIntroEnable(Constants.JSON_FULL_INTRO_2)) {
//            if (listFragment.size == 3) {
//                listFragment.add(2, NativeFragment.newInstance(2, onNativeFragClick))
//            } else if (listFragment.size == 4) {
//                listFragment.add(3, NativeFragment.newInstance(2, onNativeFragClick))
//            }
//        }
//    }

    override fun createFragment(position: Int): Fragment = listFragment[position]

    override fun getItemCount(): Int = listFragment.size

//    private fun checkNativeIntroEnable(json: String): Boolean {
//        if (json.isBlank() || !AdmobUtils.isNetworkConnected(activity)) {
//            return false
//        }
//
//        val adsConfig =
//            try {
//                Gson().fromJson(json, AdsNativeConfig::class.java)
//            } catch (e: Exception) {
//                return false
//            }
//
//        return adsConfig.type != "0"
//    }
//
//    private fun checkNativeLayout(
//        json: String,
//        position: Int,
//    ): Boolean {
//        if (json.isBlank() || !AdmobUtils.isNetworkConnected(activity) || AdmobUtils.isTestDevice) {
//            return false
//        }
//        val adsConfig =
//            try {
//                Gson().fromJson(json, AdsNativeConfig::class.java)
//            } catch (e: Exception) {
//                return false
//            }
//
//        return adsConfig.type != "0"
//    }
}
