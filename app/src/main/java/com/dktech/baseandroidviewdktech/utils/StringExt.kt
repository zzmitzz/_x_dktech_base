package com.dktech.baseandroidviewdktech.utils

import android.view.View


fun String.isEmailValid(): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun View.getStringResource(id: Int): String = resources.getString(id)