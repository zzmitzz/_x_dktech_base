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
    fun loadImage(
        painting: PaintingUIWrapper,
        view: ImageView,
        shimmer: ShimmerFrameLayout?,
    ) {
        shimmer?.apply {
            this.visibility = View.VISIBLE
            startShimmer()
        }

        val request =
            when {
                painting.cacheThumb != null -> {
                    val file = painting.cacheThumb.toUri().toFile()
                    if (file.exists()) {
                        Glide
                            .with(view)
                            .load(file)
                            .signature(ObjectKey(file.lastModified()))
                    } else {
                        Glide.with(view).load(painting.remoteThumb)
                    }
                }

                else -> {
                    Glide.with(view).load(painting.remoteThumb)
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
                        shimmer?.stopShimmer()
                        shimmer?.setShimmer(null)
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
                        shimmer?.setShimmer(null)
                        return false
                    }
                },
            ).into(view)
    }
}
