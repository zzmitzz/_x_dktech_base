package com.dktech.baseandroidviewdktech.svgparser.model

data class SVGInfo(
    val width: Int,
    val height: Int,
    val viewBox: String,
    val paths: List<SegmentGroup>,
)
