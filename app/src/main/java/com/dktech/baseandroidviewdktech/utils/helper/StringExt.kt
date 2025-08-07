package com.dktech.baseandroidviewdktech.utils.helper

import android.util.Patterns
import android.view.View


fun String.isEmailValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun View.getStringResource(id: Int): String = resources.getString(id)