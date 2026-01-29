package com.dktech.baseandroidviewdktech.utils.helper

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

object CustomLoadingImage {
    private const val MAX_RETRY_COUNT = 3

    fun loadImage(
        painting: PaintingUIWrapper,
        view: ImageView,
        shimmer: ShimmerFrameLayout? = null,
        underLayer: View? = null,
        retryCount: Int = 0,
    ) {
        Glide.with(view).clear(view)

        shimmer?.apply {
            this.visibility = View.VISIBLE
            startShimmer()
            underLayer?.visible()
        }

        val imageUrl = determineImageSource(painting)

        if (imageUrl == null) {
            handleLoadFailure(shimmer, underLayer)
            return
        }

        val request =
            when {
                painting.cacheThumb != null -> {
                    val file = painting.cacheThumb.toUri().toFile()
                    if (file.exists()) {
                        val lastModified = file.lastModified()
                        Glide
                            .with(view)
                            .load(file)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .signature(ObjectKey(lastModified))
                    } else {
                        Glide.with(view).load(painting.remoteThumb)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                    }
                }

                else -> {
                    Glide.with(view).load(painting.remoteThumb)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                }
            }

        request
            .dontAnimate()
            .listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        if (retryCount < MAX_RETRY_COUNT) {
                            view.postDelayed({
                                loadImage(painting, view, shimmer, underLayer, retryCount + 1)
                            }, calculateRetryDelay(retryCount))
                        } else {
                            handleLoadFailure(shimmer, underLayer)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        shimmer?.stopShimmer()
                        underLayer?.gone()
                        shimmer?.setShimmer(null)
                        return false
                    }
                },
            ).into(view)
    }

    private fun determineImageSource(painting: PaintingUIWrapper): String? =
        when {
            painting.cacheThumb != null -> {
                val file = painting.cacheThumb.toUri().toFile()
                if (file.exists()) painting.cacheThumb else painting.remoteThumb
            }

            else -> {
                painting.remoteThumb
            }
        }

    private fun handleLoadFailure(
        shimmer: ShimmerFrameLayout?,
        underLayer: View?,
    ) {
        shimmer?.stopShimmer()
        underLayer?.gone()
        shimmer?.setShimmer(null)
    }

    private fun calculateRetryDelay(retryCount: Int): Long = (500L * (retryCount + 1))
}
