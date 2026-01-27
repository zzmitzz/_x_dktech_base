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

class IntroBFragment : Fragment() {
    private var nextCallback: (() -> Unit)? = null
    private var onNativeAds: Boolean = false

    companion object {
        fun newInstance(
            onNativeAds: Boolean,
            nextCallback: () -> Unit,
        ): IntroBFragment {
            val fragment = IntroBFragment()
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
            binding.textView.text = getString(R.string.intro_2_title)
            binding.textView2.text = getString(R.string.intro_2_desc)
            binding.btnNext.text = getString(R.string.next)
            binding.textView.invalidate()
            binding.dot.setImageResource(R.drawable.dot_2)
            binding.imageView.setImageResource(R.drawable.intro_2)
//            AdsManager.showNativePreload(requireActivity(), RemoteConfig.NATIVE_INTRO2_231225, binding.frNative)
        } else {
            bindingNoAds.btnNext.setSafeOnClickListener {
                nextCallback?.invoke()
            }
            bindingNoAds.textView.text = getString(R.string.intro_2_title)
            bindingNoAds.textView2.text = getString(R.string.intro_2_desc)
            bindingNoAds.btnNext.text = getString(R.string.next)
            bindingNoAds.textView.invalidate()
            bindingNoAds.dot.setImageResource(R.drawable.dot_2)
            bindingNoAds.imageView.setImageResource(R.drawable.intro_2_full)
        }
    }
}
