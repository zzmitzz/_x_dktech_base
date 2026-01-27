package com.dktech.baseandroidviewdktech.ui.intro.fragments

import com.dktech.baseandroidviewdktech.base.BaseFragment
import com.dktech.baseandroidviewdktech.databinding.FragmentNativeBinding
import com.dktech.baseandroidviewdktech.utils.helper.visible

class NativeFragment : BaseFragment<FragmentNativeBinding>() {
    private var nextAction: () -> Unit = {}
    private var position: Int = 1

    companion object {
        fun newInstance(
            position: Int,
            nextAction: () -> Unit,
        ): NativeFragment =
            NativeFragment().apply {
                this.position = position
                this.nextAction = nextAction
            }
    }

    override fun getViewBinding(): FragmentNativeBinding = FragmentNativeBinding.inflate(layoutInflater)

    override fun initView() {
        binding.layoutNativeFull.visible()
//        if (position == 1) {
//            AdsManager.showNativeFullPreload(
//                requireActivity(),
//                RemoteConfig.NATIVE_FC_INTRO1_231225,
//                binding.frNativeFull,
//                R.layout.ad_template_native_fullscreen,
//            )
//        } else {
//            AdsManager.showNativeFullPreload(
//                requireActivity(),
//                RemoteConfig.NATIVE_FC_INTRO_2_231225,
//                binding.frNativeFull,
//                R.layout.ad_template_native_fullscreen,
//            )
//        }
        binding.icClose.visible()
        binding.icClose.setOnClickListener {
            nextAction()
        }
    }

    override fun initData() {
    }

    override fun initEvent() {
    }
}
