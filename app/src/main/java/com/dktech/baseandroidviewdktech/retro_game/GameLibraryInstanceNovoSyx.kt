package com.emulator.retro.console.game.retro_game

import android.app.Application
import com.bigbratan.emulair.common.activities.game.GameLoader
import com.bigbratan.emulair.common.managers.bios.BiosManager
import com.bigbratan.emulair.common.managers.coresLibrary.CoreVariablesManager
import com.bigbratan.emulair.common.managers.coresLibrary.CoresSelection
import com.bigbratan.emulair.common.managers.preferences.SharedPreferencesHelper
import com.bigbratan.emulair.common.managers.saves.SavesCoherencyEngine
import com.bigbratan.emulair.common.managers.saves.SavesManager
import com.bigbratan.emulair.common.managers.saves.StatesManager
import com.bigbratan.emulair.common.managers.saves.StatesPreviewManager
import com.bigbratan.emulair.common.managers.storage.DirectoriesManager
import com.bigbratan.emulair.common.managers.storage.StorageProviderRegistry
import com.bigbratan.emulair.common.managers.storage.local.LocalStorageProvider
import com.bigbratan.emulair.common.managers.storage.local.StorageAccessFrameworkProvider
import com.bigbratan.emulair.common.metadata.retrograde.EmulairLibrary
import com.bigbratan.emulair.common.metadata.retrograde.db.RetrogradeDatabase
//import com.bigbratan.emulair.ext.managers.review.ReviewManager
import com.emulator.retro.console.game.data.local.LibretroDatabase
import com.emulator.retro.console.game.retro_game.launcher.GameLaunchTaskHandlerNovoSyx
import com.emulator.retro.console.game.retro_game.launcher.GameLauncherNovoSyx
import com.emulator.retro.console.game.retro_game.manager.input.ControllerConfigsManagerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.input.InputDeviceManagerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.rumble.RumbleManagerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.settings.SettingsManagerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.state.MyStatesHelperNovoSyx

class GameLibraryInstanceNovoSyx(private val application: Application) {

    companion object {
        @Volatile
        private var INSTANCE: GameLibraryInstanceNovoSyx? = null

        fun getInstance(mApplication: Application): GameLibraryInstanceNovoSyx {
            return INSTANCE ?: synchronized(this) {
                val instance = GameLibraryInstanceNovoSyx(mApplication)
                INSTANCE = instance

                instance
            }
        }
    }
    val mRetrogradeDb by lazy {
        RetrogradeDatabase.Companion.getDatabase(application)
    }

    val mLibretroDb by lazy {
        LibretroDatabase.Companion.getDatabase(application)
    }

    val mSharedPref by lazy {
        SharedPreferencesHelper.getSharedPreferences(application)
    }

    val mDirectionManager by lazy {
        DirectoriesManager(application)
    }

    val mBiosManager by lazy {
        BiosManager(mDirectionManager)
    }

    val mSaverManager by lazy {
        SavesManager(mDirectionManager)
    }

    val mStatesManager by lazy {
        StatesManager(mDirectionManager)
    }

    val mCoreVariablesManager by lazy {
        CoreVariablesManager(mSharedPref)
    }

    val mSavesCoherencyEngine by lazy {
        SavesCoherencyEngine(mSaverManager, mStatesManager)
    }

    val mGameMetadataProvider by lazy {
        LibretroDBMetadataProviderNovoSyx(mLibretroDb)
    }

    val mLocalSAFStorageProvider by lazy {
        StorageAccessFrameworkProvider(application)
    }

    val mLocalGameStorageProvider by lazy {
        LocalStorageProvider(application, mDirectionManager)
    }

    // TODO: JvmSuppressWildcards providers ???
    val mGameStorageProviderRegistry by lazy {
        StorageProviderRegistry(
            application,
            setOf(mLocalSAFStorageProvider, mLocalGameStorageProvider)
        )
    }

    val mEmulairLibrary by lazy {
        EmulairLibrary(
            mRetrogradeDb,
            mGameStorageProviderRegistry,
            mGameMetadataProvider,
            mBiosManager
        )
    }

    val mGameLoader by lazy {
        GameLoader(
            mEmulairLibrary,
            mStatesManager,
            mSaverManager,
            mCoreVariablesManager,
            mRetrogradeDb,
            mSavesCoherencyEngine,
            mDirectionManager,
            mBiosManager,
        )
    }

    val mCoresSelection by lazy {
        CoresSelection(mSharedPref)
    }

    val mControllerConfigsManager by lazy {
        ControllerConfigsManagerNovoSyx(mSharedPref)
    }

    val mSettingManager by lazy { SettingsManagerNovoSyx(application, mSharedPref) }
    val mInputDeviceManager by lazy { InputDeviceManagerNovoSyx(application, mSharedPref) }

    val mRumbleManager by lazy {
        RumbleManagerNovoSyx(application, mSettingManager, mInputDeviceManager)
    }

    val mStatesPreviewManager by lazy {
        StatesPreviewManager(mDirectionManager)
    }

//    private val mReviewManager by lazy {
//        ReviewManager()
//    }

    val mStatesHelper by lazy {
        MyStatesHelperNovoSyx(mStatesManager, mStatesPreviewManager)
    }

    private val mGameLaunchTaskHandler by lazy {
//        GameLaunchTaskHandler(mReviewManager, mRetrogradeDb)
        GameLaunchTaskHandlerNovoSyx(mRetrogradeDb)
    }

    val mGameLauncher by lazy {
        GameLauncherNovoSyx(mCoresSelection, mGameLaunchTaskHandler)
    }
}