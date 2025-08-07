package com.dktech.baseandroidviewdktech.utils.helper

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.dktech.baseandroidviewdktech.R

private const val DEBOUNCE_CLICK = 500L


// -----------------------------------------------------------------------------
// VIEW EXTENSION
fun View.gone(){
    visibility = View.GONE
}

fun View.visible(){
    visibility = View.VISIBLE
}

fun View.invisible(){
    visibility = View.INVISIBLE
}

fun View.setSafeOnClickListener(
    delay: Long = DEBOUNCE_CLICK,
    onClick: (View) -> Unit
) {
    var lastClickTime: Long = 0L
    setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < delay) {
            onClick(it)
            lastClickTime = now
        }
    }
}


/*
 - enable: view can interact, the state is defined enable,
 */
fun View.ableToClick(
    enable: Boolean = true,
    disableView: () -> Unit,
    enableView: () -> Unit
) {
    if (enable) {
        enableView()
        isClickable = true
        isFocusable = true
    } else {
        disableView()
        isClickable = false
        isFocusable = false
    }
}


// -----------------------------------------------------------------------------
// IMAGE VIEW EXTENSION


fun ImageView.loadRemoteImage(
    context: Context,
    url: String,
) {
    Glide
        .with(context)
        .load(url)
        .apply {
            RequestOptions().placeholder(R.drawable.img_thumbnail).priority(Priority.HIGH)
        }
        .thumbnail(
            Glide.with(context)
                .load(url)
                .sizeMultiplier(0.2f)
                .override(width, height)
        )
        .transition(DrawableTransitionOptions.withCrossFade())
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .error(R.drawable.img_thumbnail)
        .into(this)

}

fun ImageView.loadRemoteImage(
    context: Context,
    url: String,
    header: Map<String, String> = emptyMap()
) {
    val glideUrl = GlideUrl(url) {header}
    Glide
        .with(context)
        .load(glideUrl)
        .apply {
            RequestOptions().placeholder(R.drawable.img_thumbnail).priority(Priority.HIGH)
        }
        .thumbnail(
            Glide.with(context)
                .load(url)
                .sizeMultiplier(0.2f)
                .override(width, height)
        )
        .transition(DrawableTransitionOptions.withCrossFade())
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .error(R.drawable.img_thumbnail)
        .into(this)
}

// -----------------------------------------------------------------------------
// TEXT VIEW EXTENSION
fun TextView.setGradientColor(
    startColor: String = "#47A6FF",
    endColor: String = "#005BFA"
) {


    val shader = LinearGradient(
        0f, 0f, this.paint.measureText(this.text.toString()), 0f,
        intArrayOf(
            Color.parseColor(startColor), // Red
            Color.parseColor(endColor)  // Cyan
        ),
        null,
        Shader.TileMode.CLAMP
    )

    this.paint.shader = shader
}

// -----------------------------------------------------------------------------
// Keyboard

fun Context.hideKeyboard(viewFocus: View) {
    if (viewFocus.isFocused) {
        getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(
            viewFocus.windowToken,
            0
        )
        viewFocus.clearFocus()
    }
}

fun Context.showKeyboard(view: View) {
    if (view.requestFocus()) {
        getSystemService(InputMethodManager::class.java).showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }
}