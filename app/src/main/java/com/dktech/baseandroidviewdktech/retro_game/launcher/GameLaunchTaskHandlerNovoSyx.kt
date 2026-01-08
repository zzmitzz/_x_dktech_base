package com.emulator.retro.console.game.retro_game.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.bigbratan.emulair.common.metadata.retrograde.db.RetrogradeDatabase
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
//import com.bigbratan.emulair.ext.managers.review.ReviewManager
import com.emulator.retro.console.game.retro_game.activities.BaseGameActivityNovoSyx
import com.emulator.retro.console.game.retro_game.activities.GameCrashActivityNovoSyx

class GameLaunchTaskHandlerNovoSyx(
//    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handleGameStart(context: Context) {
        cancelBackgroundWork(context)
    }

    suspend fun handleGameFinish(
        enableRatingFlow: Boolean,
        activity: Activity,
        resultCode: Int,
        data: Intent?
    ) {
        rescheduleBackgroundWork(activity.applicationContext)
        when (resultCode) {
            Activity.RESULT_OK -> handleSuccessfulGameFinish(activity, enableRatingFlow, data)
            BaseGameActivityNovoSyx.RESULT_ERROR -> handleUnsuccessfulGameFinish(
                activity,
                data?.getStringExtra(BaseGameActivityNovoSyx.PLAY_GAME_RESULT_ERROR)!!,
                null
            )

            BaseGameActivityNovoSyx.RESULT_UNEXPECTED_ERROR -> handleUnsuccessfulGameFinish(
                activity,
                "It looks like the Libretro core has experienced an unexpected issue. If it\\'s persistent please try the following:\\n\\n" +
                        "        \\n\\u2022 Disable \"Save state on correct quit\" and re-run the game\\n" +
                        "        \\n\\u2022 Go into Android settings and clear the app cache\\n" +
                        "        \\n\\u2022 Go into Emulair settings and perform a Factory Reset\\n" +
                        "        \\n\\n If none of the above works, it might be possible that this game/core is not supported by this device.",
                data?.getStringExtra(BaseGameActivityNovoSyx.PLAY_GAME_RESULT_ERROR)
            )
        }
    }

    private fun cancelBackgroundWork(context: Context) {
//        SaveSyncWork.cancelAutoWork(context)
//        SaveSyncWork.cancelManualWork(context)
//        CacheCleanerWork.cancelCleanCacheLRU(context)
    }

    private fun rescheduleBackgroundWork(context: Context) {
        // Let's slightly delay the sync. Maybe the user wants to play another game.
//        SaveSyncWork.enqueueAutoWork(context, 5)
//        CacheCleanerWork.enqueueCleanCacheLRU(context)
    }

    private fun handleUnsuccessfulGameFinish(
        activity: Activity,
        message: String,
        messageDetail: String?
    ) {
        GameCrashActivityNovoSyx.launch(activity, message, messageDetail)
    }

    private suspend fun handleSuccessfulGameFinish(
        activity: Activity,
        enableRatingFlow: Boolean,
        data: Intent?
    ) {
        val duration = data?.extras?.getLong(BaseGameActivityNovoSyx.PLAY_GAME_RESULT_SESSION_DURATION)
            ?: 0L
        val game = data?.extras?.getSerializable(BaseGameActivityNovoSyx.PLAY_GAME_RESULT_GAME) as Game

        updateGamePlayedTimestamp(game)
//        if (enableRatingFlow) {
//            displayReviewRequest(activity, duration)
//        }
    }

//    private suspend fun displayReviewRequest(activity: Activity, durationMillis: Long) {
//        delay(500)
//        reviewManager.launchReviewFlow(activity, durationMillis)
//    }

    private suspend fun updateGamePlayedTimestamp(game: Game) {
        retrogradeDb.gameDao().update(game.copy(lastPlayedAt = System.currentTimeMillis()))
    }
}
