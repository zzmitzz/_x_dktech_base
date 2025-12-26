package com.dktech.baseandroidviewdktech.base.ui_models

import com.dktech.baseandroidviewdktech.R

data class LanguageModel(
    val flagImage: Int,
    val name: Int,
    val key: String
)

fun getLanguageList(): List<LanguageModel> = listOf(
    LanguageModel(R.drawable.ic_english, R.string.english, "en"),
    LanguageModel(R.drawable.ic_hindi, R.string.hindi, "hi"),
    LanguageModel(R.drawable.ic_spanish, R.string.spanish, "es"),
    LanguageModel(R.drawable.ic_french, R.string.french, "fr"),
    LanguageModel(R.drawable.ic_arabic, R.string.arabic, "ar"),
    LanguageModel(R.drawable.ic_bengali, R.string.bengali, "bn"),
    LanguageModel(R.drawable.ic_german, R.string.german, "de"),
    LanguageModel(R.drawable.ic_italian, R.string.italian, "it"),
    LanguageModel(R.drawable.ic_korean_flag, R.string.korean, "ko"),
    LanguageModel(R.drawable.ic_portuguese, R.string.portuguese, "pt"),
    LanguageModel(R.drawable.ic_russian, R.string.russian, "ru"),
    LanguageModel(R.drawable.ic_indonesian, R.string.indonesian, "id"),
)