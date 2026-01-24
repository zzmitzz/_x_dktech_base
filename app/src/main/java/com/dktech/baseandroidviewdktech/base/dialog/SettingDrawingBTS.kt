package com.dktech.baseandroidviewdktech.base.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dktech.baseandroidviewdktech.databinding.DialogBtsSettingDrawBinding
import com.dktech.baseandroidviewdktech.utils.Constants
import com.dktech.baseandroidviewdktech.utils.helper.getBooleanPrefs
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingDrawingBTS constructor(
    val initState: Pair<Boolean, Boolean> = false to false,
    val onSave: (Pair<Boolean, Boolean>) -> Unit = {},
) : BottomSheetDialogFragment() {
    private var binding: DialogBtsSettingDrawBinding? = null
    private var isPreviewOpen: Boolean = false
    private var isVibrateEnable: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogBtsSettingDrawBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        isPreviewOpen = this@SettingDrawingBTS.requireContext().getBooleanPrefs(Constants.configPreview)
        isVibrateEnable = this@SettingDrawingBTS.requireContext().getBooleanPrefs(Constants.configVibration)
        updateSwitch()
    }

    private fun updateSwitch() {
        binding?.switchPreview?.setChecked(isPreviewOpen, animate = false)
        binding?.switchVibrate?.setChecked(isVibrateEnable, animate = false)
    }

    private fun initEvent() {
        binding?.apply {
            imageView3.setSafeOnClickListener {
                dismiss()
            }
            btnSave.setSafeOnClickListener {
                onSave(
                    isPreviewOpen to isVibrateEnable,
                )
                dismiss()
            }
            switchPreview.setOnCheckedChangeListener { checked ->
                isPreviewOpen = checked
            }
            switchVibrate.setOnCheckedChangeListener { checked ->
                isVibrateEnable = checked
            }
        }
    }
}
