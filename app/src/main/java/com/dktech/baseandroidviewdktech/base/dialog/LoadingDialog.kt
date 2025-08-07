package com.dktech.baseandroidviewdktech.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable

class LoadingDialog(
    context: Context,
    layoutID: Int = 1
)  {
    private var dialog: Dialog? = null
    private var run: Runnable? = null
    private var handler: Handler? = null

    init {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(layoutID)
        val window: Window? = dialog?.window
        window?.let {
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            it.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            it.attributes = layoutParams

            dialog?.setCancelable(false)

            handler = Handler(Looper.getMainLooper())
            run = Runnable {
                try {
                    if (dialog != null && dialog?.isShowing == true) {
                        dialog?.hide()
                    }
                } catch (e: Exception) {
                    ////LogVnp.Shape1(Shape1);
                }
            }
        }

    }

    fun show() {
        dialog?.show()
        run?.let { handler?.postDelayed(it, 90000) }
    }

    fun hide() {
        dialog?.dismiss()

        try {
            run?.let { handler?.removeCallbacks(it) }
        } catch (e: Exception) {

        }
    }

}