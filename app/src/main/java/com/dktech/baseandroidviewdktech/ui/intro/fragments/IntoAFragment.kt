package com.dktech.baseandroidviewdktech.ui.intro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.databinding.FragmentIntroBinding
import com.dktech.baseandroidviewdktech.databinding.FragmentIntroFullBinding
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class IntoAFragment : Fragment() {
    private var nextCallback: (() -> Unit)? = null
    private var onNativeAds: Boolean = false

    companion object {
        fun newInstance(
            onNativeAds: Boolean,
            nextCallback: () -> Unit,
        ): IntoAFragment {
            val fragment = IntoAFragment()
            fragment.nextCallback = nextCallback
            fragment.onNativeAds = onNativeAds
            return fragment
        }
    }

    private val binding by lazy {
        FragmentIntroBinding.inflate(layoutInflater)
    }

    private val bindingNoAds by lazy {
        FragmentIntroFullBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        if (onNativeAds) {
            binding.root
        } else {
            bindingNoAds.root
        }

    private var preventInter = true

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (onNativeAds) {
            if (preventInter) {
                preventInter = false
                binding.btnNext.setSafeOnClickListener {
                    nextCallback?.invoke()
                }
            }

            binding.textView.text = getString(R.string.relax_with_color_by_number)
            binding.textView2.text = getString(R.string.desc_intro_1)
            binding.btnNext.text = getString(R.string.next)
            binding.textView.invalidate()
            binding.dot.setImageResource(R.drawable.dot_1)
            binding.imageView.setImageResource(R.drawable.intro_1)
//            AdsManager.showNativePreload(requireActivity(), RemoteConfig.NATIVE_INTRO1_231225, binding.frNative)
        } else {
            bindingNoAds.btnNext.setSafeOnClickListener {
                nextCallback?.invoke()
            }
            bindingNoAds.textView.text = getString(R.string.relax_with_color_by_number)
            bindingNoAds.textView2.text = getString(R.string.desc_intro_1)
            bindingNoAds.btnNext.text = getString(R.string.next)
            bindingNoAds.textView.invalidate()
            bindingNoAds.dot.setImageResource(R.drawable.dot_1)
            bindingNoAds.imageView.setImageResource(R.drawable.intro_1_full)
        }
    }
}
