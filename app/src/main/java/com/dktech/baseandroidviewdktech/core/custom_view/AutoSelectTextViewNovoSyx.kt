package com.emulator.retro.console.game.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AutoSelectTextViewNovoSyx(mContext: Context, attrs: AttributeSet? = null): AppCompatTextView(mContext, attrs) {

    init { isSelected = true }

}