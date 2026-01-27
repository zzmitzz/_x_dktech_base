package com.dktech.baseandroidviewdktech.base.dialog

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.databinding.DialogCollectionBinding

class CollectionDialog constructor(
    val isCompletedArt: Boolean = false,
    val cacheImage: Uri,
    val listenerCallback: CollectionDialog.OnCallbackAction,
) : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullWidthDialog)
    }

    interface OnCallbackAction {
        fun onNextAction()

        fun onResetAction()

        fun onDeleteAction()
    }

    private var binding: DialogCollectionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogCollectionBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
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
        binding?.apply {
            if (isCompletedArt) {
                icAction.setImageResource(R.drawable.ic_share)
                tvAction.text = getString(R.string.share)
            } else {
                icAction.setImageResource(R.drawable.ic_edit)
                tvAction.text = getString(R.string.continue_action)
            }
        }
        binding?.let {
            Glide
                .with(this)
                .load(cacheImage)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding!!.imCache)
        }
    }

    private fun initEvent() {
        binding?.apply {
            icClose.setOnClickListener {
                dismiss()
            }
            btnAction.setOnClickListener {
                listenerCallback.onNextAction()
                dismiss()
            }
            btnReset.setOnClickListener {
                listenerCallback.onResetAction()
                dismiss()
            }
            btnDelete.setOnClickListener {
                listenerCallback.onDeleteAction()
                dismiss()
            }
        }
    }
}
