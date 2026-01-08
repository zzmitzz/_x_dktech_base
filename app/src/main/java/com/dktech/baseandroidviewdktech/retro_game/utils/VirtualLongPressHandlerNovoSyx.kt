package com.emulator.retro.console.game.retro_game.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.isVisible
import com.bigbratan.emulair.common.utils.graphics.GraphicsUtils
import com.bigbratan.emulair.common.utils.view.animateProgress
import com.bigbratan.emulair.common.utils.view.animateVisibleOrGone
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.retro_game.activities.GameActivityNovoSyx
import com.swordfish.radialgamepad.library.config.MyDuplicatedRGPTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

object VirtualLongPressHandlerNovoSyx {

    private val APPEAR_ANIMATION = (ViewConfiguration.getLongPressTimeout() * 0.1f).toLong()
    private val DISAPPEAR_ANIMATION = (ViewConfiguration.getLongPressTimeout() * 2f).toLong()
    private val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong()

    fun initializeTheme(gameActivity: GameActivityNovoSyx, palette: MyDuplicatedRGPTheme) {
        longPressIconView(gameActivity).setColorFilter(palette.textColor)
        longPressProgressBar(gameActivity).setIndicatorColor(palette.textColor)
        longPressView(gameActivity).background = buildCircleDrawable(
            gameActivity,
            palette.backgroundColor,
            palette.backgroundStrokeColor,
            palette.strokeWidthDp
        )
        longPressForegroundView(gameActivity).background = buildCircleDrawable(
            gameActivity,
            palette.normalColor,
            palette.normalStrokeColor,
            palette.strokeWidthDp
        )
    }

    private fun buildCircleDrawable(
        context: Context,
        fillColor: Int,
        strokeColor: Int,
        strokeSize: Float
    ): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
            setStroke(GraphicsUtils.convertDpToPixel(strokeSize, context).roundToInt(), strokeColor)
        }
    }

    suspend fun displayLoading(
        activity: GameActivityNovoSyx,
        iconId: Int,
        cancellation: Flow<Unit>
    ): Boolean {
        withContext(Dispatchers.Main) {
            longPressView(activity).alpha = 0f
            longPressIconView(activity).setImageResource(iconId)
            displayLongPressView(activity)
        }

        val successFlow = flow {
            delay(LONG_PRESS_TIMEOUT)
            emit(true)
        }

        val cancellationFlow = cancellation.map { false }

        val isSuccessful = merge(successFlow, cancellationFlow)
            .first()

        withContext(Dispatchers.Main) {
            if (isSuccessful) {
                longPressView(activity).isVisible = false
            }
            hideLongPressView(activity)
        }

        return isSuccessful
    }

    private fun longPressIconView(activity: GameActivityNovoSyx) =
        activity.binding.settingsLoading.settingsLoadingIcon

    private fun longPressProgressBar(activity: GameActivityNovoSyx) =
        activity.binding.settingsLoading.settingsLoadingProgress

    private fun longPressView(activity: GameActivityNovoSyx) = activity.binding.settingsLoading.root

    private fun longPressForegroundView(activity: GameActivityNovoSyx) =
        activity.findViewById<View>(R.id.long_press_foreground)

    private fun displayLongPressView(activity: GameActivityNovoSyx) {
        longPressView(activity).animateVisibleOrGone(true, APPEAR_ANIMATION)
        longPressProgressBar(activity).animateProgress(100, LONG_PRESS_TIMEOUT)
    }

    private fun hideLongPressView(activity: GameActivityNovoSyx) {
        longPressView(activity).animateVisibleOrGone(false, DISAPPEAR_ANIMATION)
        longPressProgressBar(activity).animateProgress(0, LONG_PRESS_TIMEOUT)
    }
}
