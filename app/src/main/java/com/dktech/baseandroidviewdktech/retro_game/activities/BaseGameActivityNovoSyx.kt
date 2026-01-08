package com.emulator.retro.console.game.retro_game.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PointF
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.bigbratan.emulair.common.activities.game.GameLoader
import com.bigbratan.emulair.common.activities.game.GameLoaderError
import com.bigbratan.emulair.common.activities.game.GameLoaderException
import com.bigbratan.emulair.common.managers.controller.ControllerConfig
import com.bigbratan.emulair.common.managers.controller.EmulairTouchOverlayThemes
import com.bigbratan.emulair.common.managers.coresLibrary.CoreVariable
import com.bigbratan.emulair.common.managers.saves.IncompatibleStateException
import com.bigbratan.emulair.common.managers.saves.SaveState
import com.bigbratan.emulair.common.managers.saves.StatesPreviewManager
import com.bigbratan.emulair.common.managers.storage.RomFiles
import com.bigbratan.emulair.common.metadata.retrograde.ExposedSetting
import com.bigbratan.emulair.common.metadata.retrograde.GameSystem
import com.bigbratan.emulair.common.metadata.retrograde.SystemCoreConfig
import com.bigbratan.emulair.common.metadata.retrograde.SystemID
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.bigbratan.emulair.common.utils.coroutines.MutableStateProperty
import com.bigbratan.emulair.common.utils.coroutines.launchOnState
import com.bigbratan.emulair.common.utils.coroutines.safeCollect
import com.bigbratan.emulair.common.utils.displayToast
import com.bigbratan.emulair.common.utils.graphics.GraphicsUtils
import com.bigbratan.emulair.common.utils.graphics.takeScreenshot
import com.bigbratan.emulair.common.utils.kotlin.NTuple2
import com.bigbratan.emulair.common.utils.kotlin.NTuple4
import com.bigbratan.emulair.common.utils.kotlin.filterNotNullValues
import com.bigbratan.emulair.common.utils.kotlin.toIndexedMap
import com.bigbratan.emulair.common.utils.kotlin.zipOnKeys
import com.bigbratan.emulair.common.utils.longAnimationDuration
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.bases.BaseActivityNovoSyx
import com.emulator.retro.console.game.retro_game.GameLibraryInstanceNovoSyx
import com.emulator.retro.console.game.retro_game.manager.input.inputClass.getInputClass
import com.emulator.retro.console.game.retro_game.menu.ForwardAction
import com.emulator.retro.console.game.retro_game.menu.GameMenu
import com.emulator.retro.console.game.retro_game.menu.IGameMenuAction
import com.emulator.retro.console.game.retro_game.menu.LoadStateAction
import com.emulator.retro.console.game.retro_game.menu.MuteAction
import com.emulator.retro.console.game.retro_game.menu.QuitAction
import com.emulator.retro.console.game.retro_game.menu.RestartAction
import com.emulator.retro.console.game.retro_game.menu.SaveStateAction
import com.emulator.retro.console.game.retro_game.pauseMenuAction.CoreOptionNovoSyx
import com.emulator.retro.console.game.retro_game.pauseMenuAction.EmulairCoreOptionNovoSyx
import com.emulator.retro.console.game.retro_game.utils.input.InputKeyNovoSyx
import com.emulator.retro.console.game.utils.CommonNovoSyx
import com.swordfish.libretrodroid.Controller
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import com.swordfish.radialgamepad.library.math.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class BaseGameActivityNovoSyx<B : ViewBinding>(
    inflater: (LayoutInflater) -> B
) : BaseActivityNovoSyx<B>(inflater) {

    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (loadingState.value) return

            if (mGameMenu.isShowed) {
                mGameMenu.hide()
                return
            }

            lifecycleScope.launch {
                autoSaveAndFinish()
            }
        }
    }

    protected val game: Game by lazy {
        intent.getSerializableExtra(EXTRA_GAME) as Game
    }
    private val systemCoreConfig: SystemCoreConfig by lazy {
        intent.getSerializableExtra(EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig
    }
    private val system: GameSystem by lazy {
        GameSystem.findById(game.systemId)
    }

    protected val mThemeWrapper by lazy {
        EmulairTouchOverlayThemes.getTheme(resources, CommonNovoSyx.Theme.iTheme)
    }

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    protected val mInstances by lazy { GameLibraryInstanceNovoSyx.getInstance(application) }

    private val startGameTime = System.currentTimeMillis()

    private val keyEventsFlow: MutableSharedFlow<KeyEvent?> = MutableSharedFlow()
    private val motionEventsFlow: MutableSharedFlow<MotionEvent> = MutableSharedFlow()

    protected val retroGameViewFlow = MutableStateFlow<GLRetroView?>(null)
    protected var retroGameView: GLRetroView? by MutableStateProperty(retroGameViewFlow)

    private val loadingState = MutableStateFlow(false)
    private val loadingMessageStateFlow = MutableStateFlow(R.string.please_wait)
    private val controllerConfigsState = MutableStateFlow<Map<Int, ControllerConfig>>(mapOf())

    override fun initData() {
        setUpExceptionsHandler()
    }

    override fun initView() {
        lifecycleScope.launch {
            loadGame()
            if (mInstances.mSettingManager.screenAutorotate()) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }

        initialiseFlows()
    }

    private fun initialiseFlows() {
        launchOnState(Lifecycle.State.CREATED) {
            initializeLoadingViewFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeControllerConfigsFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadShortcutsFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadKeysFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeVirtualGamePadMotionsFlow()
        }

        launchOnState(Lifecycle.State.STARTED) {
            initializeRetroGameViewErrorsFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeGamePadMotionsFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeLoadingMessageFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeLoadingVisibilityFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeRumbleFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeCoreVariablesFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeControllersConfigFlow()
        }
    }

    private suspend fun initializeControllersConfigFlow() {
        try {
            waitRetroGameViewInitialized()
            val controllers = mInstances.mControllerConfigsManager.getControllerConfigs(system.id, systemCoreConfig)
            controllerConfigsState.value = controllers
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun initializeRetroGameViewErrorsFlow() {
        retroGameViewFlow().getGLRetroErrors()
            .catch { Timber.e(it, "Exception in GLRetroErrors. Ironic.") }
            .collect { handleRetroViewError(it) }
    }

    private suspend fun initializeCoreVariablesFlow() {
        try {
            waitRetroGameViewInitialized()
            val options = mInstances.mCoreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
            updateCoreVariables(options)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun initializeLoadingVisibilityFlow() {
        loadingState
            .debounce(longAnimationDuration().toLong())
            .safeCollect(::showLLoading)
    }

    private suspend fun initializeLoadingMessageFlow() {
        loadingMessageStateFlow
            .debounce(2 * longAnimationDuration().toLong())
            .safeCollect(::setTxtLoading)
    }

    private suspend fun initializeControllerConfigsFlow() {
        waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
        controllerConfigsState.safeCollect {
            updateControllers(it)
        }
    }

    private suspend inline fun <reified T> waitGLEvent() {
        val retroView = retroGameViewFlow()
        retroView.getGLRetroEvents()
            .filterIsInstance<T>()
            .first()
    }

    private suspend fun waitRetroGameViewInitialized() {
        retroGameViewFlow()
    }

    private suspend fun initializeRumbleFlow() {
        val retroGameView = retroGameViewFlow()
        val rumbleEvents = retroGameView.getRumbleEvents()
        mInstances.mRumbleManager.collectAndProcessRumbleEvents(systemCoreConfig, rumbleEvents)
    }

    private fun setUpExceptionsHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("BaseGameActivityTAG", "setUpExceptionsHandler: ", exception)
            performUnexpectedErrorFinish(exception)
            defaultExceptionHandler?.uncaughtException(thread, exception)
        }
    }

    fun getControllerType(): Flow<Map<Int, ControllerConfig>> {
        return controllerConfigsState
    }

    // On some cores, unserialize fails with no reason. That's why we need to try multiple times.
    private suspend fun restoreAutoSaveAsync(saveState: SaveState) {
        /*
        PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        the autosave. Do not change thread here. Stick to the GL one to avoid issues with PPSSPP.
        */
        if (!isAutoSaveEnabled()) return

        try {
            waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
            restoreQuickSave(saveState)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading auto-save")
        }
    }

    private suspend fun takeScreenshotPreview(index: Int) {
        val sizeInDp = StatesPreviewManager.PREVIEW_SIZE_DP
        val previewSize = GraphicsUtils.convertDpToPixel(sizeInDp, applicationContext).roundToInt()
        val preview = retroGameView?.takeScreenshot(previewSize, 3)
        if (preview != null) {
            mInstances.mStatesPreviewManager.setPreviewForSlot(game, preview, systemCoreConfig.coreID, index)
        }
    }

    private fun initializeRetroGameView(
        gameData: GameLoader.GameData,
        screenFilter: String,
        lowLatencyAudio: Boolean,
        enableRumble: Boolean
    ): GLRetroView {
        val data = GLRetroViewData(this).apply {
            coreFilePath = gameData.coreLibrary

            when (val gameFiles = gameData.gameFiles) {
                is RomFiles.Standard -> {
                    gameFilePath = gameFiles.files.first().absolutePath
                }

                is RomFiles.Virtual -> {
                    gameVirtualFiles = gameFiles.files
                        .map { VirtualFile(it.filePath, it.fd) }
                }
            }

            systemDirectory = gameData.systemDirectory.absolutePath
            savesDirectory = gameData.savesDirectory.absolutePath
            variables = gameData.coreVariables.map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = gameData.saveRAMData
            shader = getShaderForSystem(screenFilter, system)
            preferLowLatencyAudio = lowLatencyAudio
            rumbleEventsEnabled = enableRumble
            skipDuplicateFrames = systemCoreConfig.skipDuplicateFrames
        }


        val retroGameView = GLRetroView(this, data)
        retroGameView.isFocusable = false
        retroGameView.isFocusableInTouchMode = false

        lifecycle.addObserver(retroGameView)
        gameContainerLayout.addView(retroGameView)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        retroGameView.layoutParams = layoutParams

        lifecycleScope.launch {
            gameData.quickSaveData?.let {
                restoreAutoSaveAsync(it)
            }
        }

        return retroGameView
    }

    private fun printRetroVariables(retroGameView: GLRetroView) {
        retroGameView.getVariables().forEach {
            Timber.i("Libretro variable: $it")
        }
    }

    private fun updateControllers(controllers: Map<Int, ControllerConfig>) {
        retroGameView
            ?.getControllers()?.toIndexedMap()
            ?.zipOnKeys(controllers, this::findControllerId)
            ?.filterNotNullValues()
            ?.forEach { (port, controllerId) ->
                Timber.i("Controls setting $port to $controllerId")
                retroGameView?.setControllerType(port, controllerId)
            }
    }

    private fun findControllerId(
        supported: Array<Controller>,
        controllerConfig: ControllerConfig
    ): Int? {
        return supported
            .firstOrNull { controller ->
                sequenceOf(
                    controller.id == controllerConfig.libretroId,
                    controller.description == controllerConfig.libretroDescriptor
                ).any { it }
            }?.id
    }

    private fun handleRetroViewError(errorCode: Int) {
        Timber.e("Error in GLRetroView $errorCode")
        val gameLoaderError = when (errorCode) {
            GLRetroView.ERROR_GL_NOT_COMPATIBLE -> GameLoaderError.GLIncompatible
            GLRetroView.ERROR_LOAD_GAME -> GameLoaderError.LoadGame
            GLRetroView.ERROR_LOAD_LIBRARY -> GameLoaderError.LoadCore
            GLRetroView.ERROR_SERIALIZATION -> GameLoaderError.Saves
            else -> GameLoaderError.Generic
        }
        retroGameView = null
        displayGameLoaderError(gameLoaderError, systemCoreConfig)
    }

    private fun transformExposedSetting(
        exposedSetting: ExposedSetting,
        coreOptions: List<CoreOptionNovoSyx>
    ): EmulairCoreOptionNovoSyx? {
        return coreOptions
            .firstOrNull { it.variable.key == exposedSetting.key }
            ?.let { EmulairCoreOptionNovoSyx(exposedSetting, it) }
    }

    protected fun displayOptionsDialog() {
        if (loadingState.value) return

        mGameMenu.setupBeforeShow(
            retroGameView?.audioEnabled?.let { !it },
            system.fastForwardSupport,
            (retroGameView?.frameSpeed ?: 1) > 1
        )
        mGameMenu.show()
    }

    private fun getShaderForSystem(screenFiter: String, system: GameSystem): ShaderConfig {
        return when (screenFiter) {
            "crt" -> ShaderConfig.CRT
            "lcd" -> ShaderConfig.LCD
            "smooth" -> ShaderConfig.Default
            "sharp" -> ShaderConfig.Sharp
            "hd" -> getHDShaderForSystem(system)
            else -> getDefaultShaderForSystem(system)
        }
    }

    private fun getDefaultShaderForSystem(system: GameSystem) =
        when (system.id) {
            SystemID.GBA -> ShaderConfig.Sharp
            SystemID.GBC -> ShaderConfig.Sharp
            SystemID.GB -> ShaderConfig.Sharp
            SystemID.N64 -> ShaderConfig.CRT
            SystemID.GENESIS -> ShaderConfig.CRT
            SystemID.SEGACD -> ShaderConfig.CRT
            SystemID.NES -> ShaderConfig.CRT
            SystemID.SNES -> ShaderConfig.CRT
            SystemID.FBNEO -> ShaderConfig.CRT
            SystemID.SMS -> ShaderConfig.CRT
            SystemID.PSP -> ShaderConfig.LCD
            SystemID.NDS -> ShaderConfig.Sharp
            SystemID.GG -> ShaderConfig.LCD
            SystemID.ATARI2600 -> ShaderConfig.CRT
            SystemID.PSX -> ShaderConfig.CRT
            SystemID.MAME2003PLUS -> ShaderConfig.CRT
            SystemID.ATARI7800 -> ShaderConfig.CRT
            SystemID.PC_ENGINE -> ShaderConfig.CRT
            SystemID.LYNX -> ShaderConfig.LCD
            SystemID.DOS -> ShaderConfig.CRT
            SystemID.NGP -> ShaderConfig.LCD
            SystemID.NGC -> ShaderConfig.LCD
            SystemID.WS -> ShaderConfig.LCD
            SystemID.WSC -> ShaderConfig.LCD
            SystemID.NINTENDO_3DS -> ShaderConfig.LCD
        }

    private fun getHDShaderForSystem(system: GameSystem): ShaderConfig {
        val upscale8Bits = ShaderConfig.CUT2(true,2.0f, 0.8f)
        val upscale16Bits = ShaderConfig.CUT2(true,1.5f, 0.8f)
        val upscale32Bits = ShaderConfig.CUT2(true,1.0f, 0.8f)
        val default = ShaderConfig.CUT2(true,0.5f, 0.8f)

        return when (system.id) {
            SystemID.GBA -> upscale16Bits
            SystemID.GBC -> upscale8Bits
            SystemID.GB -> upscale8Bits
            SystemID.N64 -> upscale32Bits
            SystemID.GENESIS -> upscale16Bits
            SystemID.SEGACD -> upscale16Bits
            SystemID.NES -> upscale8Bits
            SystemID.SNES -> upscale16Bits
            SystemID.FBNEO -> upscale32Bits
            SystemID.SMS -> upscale8Bits
            SystemID.PSP -> default
            SystemID.NDS -> upscale16Bits
            SystemID.GG -> upscale8Bits
            SystemID.ATARI2600 -> upscale8Bits
            SystemID.PSX -> upscale32Bits
            SystemID.MAME2003PLUS -> upscale32Bits
            SystemID.ATARI7800 -> upscale8Bits
            SystemID.PC_ENGINE -> upscale16Bits
            SystemID.LYNX -> upscale8Bits
            SystemID.DOS -> upscale32Bits
            SystemID.NGP -> upscale8Bits
            SystemID.NGC -> upscale8Bits
            SystemID.WS -> upscale16Bits
            SystemID.WSC -> upscale16Bits
            SystemID.NINTENDO_3DS -> default
        }
    }

    private suspend fun isAutoSaveEnabled(): Boolean {
        return systemCoreConfig.statesSupported && mInstances.mSettingManager.autoSave()
    }

    private fun getCoreOptions(): List<CoreOptionNovoSyx> {
        return retroGameView?.getVariables()
            ?.map { CoreOptionNovoSyx.fromLibretroDroidVariable(it) } ?: listOf()
    }

    private fun updateCoreVariables(options: List<CoreVariable>) {
        val updatedVariables = options.map { Variable(it.key, it.value) }
            .toTypedArray()

        updatedVariables.forEach {
            Timber.i("Updating core variable: ${it.key} ${it.value}")
        }

        retroGameView?.updateVariables(*updatedVariables)
    }

    // Now that we wait for the first rendered frame, this is probably no longer needed, but we'll keep it just to be sure
    private suspend fun restoreQuickSave(saveState: SaveState) {
        var times = 10

        while (!loadSaveState(saveState) && times > 0) {
            delay(200)
            times--
        }
    }

    private suspend fun initializeGamePadShortcutsFlow() {
        mInstances.mInputDeviceManager.getInputMenuShortCutObservable()
            .distinctUntilChanged()
            .safeCollect { shortcut ->
                shortcut?.let {
                    displayToast(
                        String.format("Gamepad detected. Access settings menu with %s.", it.name)
                    )
                }
            }
    }

    data class SingleAxisEvent(val axis: Int, val action: Int, val keyCode: Int, val port: Int)

    private suspend fun initializeVirtualGamePadMotionsFlow() {
        val events = combine(
            mInstances.mInputDeviceManager.getGamePadsPortMapperObservable(),
            motionEventsFlow,
            ::NTuple2
        )

        events
            .mapNotNull { (ports, event) ->
                ports(event.device)?.let { it to event }
            }
            .map { (port, event) ->
                val axes = event.device.getInputClass().getAxesMap().entries

                axes.map { (axis, button) ->
                    val action = if (event.getAxisValue(axis) > 0.5) {
                        KeyEvent.ACTION_DOWN
                    } else {
                        KeyEvent.ACTION_UP
                    }
                    SingleAxisEvent(axis, action, button, port)
                }.toSet()
            }
            .scan(emptySet<SingleAxisEvent>()) { prev, next ->
                next.minus(prev).forEach {
                    retroGameView?.sendKeyEvent(it.action, it.keyCode, it.port)
                }
                next
            }
            .safeCollect { }
    }

    private suspend fun initializeGamePadMotionsFlow() {
        val events = combine(
            mInstances.mInputDeviceManager.getGamePadsPortMapperObservable(),
            motionEventsFlow,
            ::NTuple2
        )

        events
            .safeCollect { (ports, event) ->
                ports(event.device)?.let {
                    sendStickMotions(event, it)
                }
            }
    }

    private suspend fun initializeGamePadKeysFlow() {
        val pressedKeys = mutableSetOf<Int>()

        val filteredKeyEvents = keyEventsFlow
            .filterNotNull()
            .filter { it.repeatCount == 0 }
            .map { Triple(it.device, it.action, it.keyCode) }
            .distinctUntilChanged()

        val shortcutKeys = mInstances.mInputDeviceManager.getInputMenuShortCutObservable()
            .map { it?.keys ?: setOf() }

        val combinedObservable = combine(
            shortcutKeys,
            mInstances.mInputDeviceManager.getGamePadsPortMapperObservable(),
            mInstances.mInputDeviceManager.getInputBindingsObservable(),
            filteredKeyEvents,
            ::NTuple4
        )

        combinedObservable
            .onStart { pressedKeys.clear() }
            .onCompletion { pressedKeys.clear() }
            .safeCollect { (shortcut, ports, bindings, event) ->
                val (device, action, keyCode) = event
                val port = ports(device)
                val bindKeyCode = bindings(device)[InputKeyNovoSyx(keyCode)]?.keyCode ?: keyCode

                if (bindKeyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
                    onBackPressedDispatcher.onBackPressed()
                    return@safeCollect
                }

                if (port == 0) {
                    if (bindKeyCode == KeyEvent.KEYCODE_BUTTON_MODE && action == KeyEvent.ACTION_DOWN) {
                        displayOptionsDialog()
                        return@safeCollect
                    }

                    if (action == KeyEvent.ACTION_DOWN) {
                        pressedKeys.add(keyCode)
                    } else if (action == KeyEvent.ACTION_UP) {
                        pressedKeys.remove(keyCode)
                    }

                    if (shortcut.isNotEmpty() && pressedKeys.containsAll(shortcut)) {
                        displayOptionsDialog()
                        return@safeCollect
                    }
                }

                port?.let {
                    retroGameView?.sendKeyEvent(action, bindKeyCode, it)
                }
            }
    }

    private fun sendStickMotions(event: MotionEvent, port: Int) {
        if (port < 0) return
        when (event.source) {
            InputDevice.SOURCE_JOYSTICK -> {
                if (controllerConfigsState.value[port]?.mergeDPADAndLeftStickEvents == true) {
                    sendMergedMotionEvents(event, port)
                } else {
                    sendSeparateMotionEvents(event, port)
                }
            }
        }
    }

    private fun sendMergedMotionEvents(event: MotionEvent, port: Int) {
        val events = listOf(
            retrieveCoordinates(event, MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y),
            retrieveCoordinates(event, MotionEvent.AXIS_X, MotionEvent.AXIS_Y)
        )

        val xVal = events.maxByOrNull { abs(it.x) }?.x ?: 0f
        val yVal = events.maxByOrNull { abs(it.y) }?.y ?: 0f

        retroGameView?.sendMotionEvent(GLRetroView.Companion.MOTION_SOURCE_DPAD, xVal, yVal, port)
        retroGameView?.sendMotionEvent(GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT, xVal, yVal, port)

        sendStickMotion(
            event,
            GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            port
        )
    }

    private fun sendSeparateMotionEvents(event: MotionEvent, port: Int) {
        sendDPADMotion(
            event,
            GLRetroView.Companion.MOTION_SOURCE_DPAD,
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            port
        )
        sendStickMotion(
            event,
            GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT,
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            port
        )
        sendStickMotion(
            event,
            GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RZ,
            port
        )
    }

    private fun sendStickMotion(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int
    ) {
        val coords = retrieveCoordinates(event, xAxis, yAxis)
        retroGameView?.sendMotionEvent(source, coords.x, coords.y, port)
    }

    private fun sendDPADMotion(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int
    ) {
        retroGameView?.sendMotionEvent(source, event.getAxisValue(xAxis), event.getAxisValue(yAxis), port)
    }

    @Deprecated("Sadly, this creates some issues with certain controllers and input lag on very slow devices.")
    private fun retrieveNormalizedCoordinates(event: MotionEvent, xAxis: Int, yAxis: Int): PointF {
        val rawX = event.getAxisValue(xAxis)
        val rawY = -event.getAxisValue(yAxis)

        val angle = MathUtils.angle(0f, rawX, 0f, rawY)
        val distance = MathUtils.clamp(MathUtils.distance(0f, rawX, 0f, rawY), 0f, 1f)

        return MathUtils.convertPolarCoordinatesToSquares(angle, distance)
    }

    private fun retrieveCoordinates(event: MotionEvent, xAxis: Int, yAxis: Int): PointF {
        return PointF(event.getAxisValue(xAxis), event.getAxisValue(yAxis))
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            GlobalScope.launch {
                motionEventsFlow.emit(event)
            }
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && InputKeyNovoSyx(keyCode) in event.device.getInputClass().getInputKeys()) {
            lifecycleScope.launch {
                keyEventsFlow.emit(event)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && InputKeyNovoSyx(keyCode) in event.device.getInputClass().getInputKeys()) {
            lifecycleScope.launch {
                keyEventsFlow.emit(event)
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private suspend fun autoSaveAndFinish() = withLoading {
        saveSRAM(game)
        saveAutoSave(game)
        performSuccessfulActivityFinish()
    }

    private fun performSuccessfulActivityFinish() {
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_SESSION_DURATION, System.currentTimeMillis() - startGameTime)
            putExtra(PLAY_GAME_RESULT_GAME, intent.getSerializableExtra(EXTRA_GAME))
        }

        setResult(RESULT_OK, resultIntent)

        finishAndExitProcess()
    }

    private inline fun withLoading(block: () -> Unit) {
        loadingState.value = true
        block()
        loadingState.value = false
    }

    private fun performUnexpectedErrorFinish(exception: Throwable) {
        Timber.e(exception, "Handling java exception in BaseGameActivity")
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_ERROR, exception.message)
        }

        setResult(RESULT_UNEXPECTED_ERROR, resultIntent)
        finishAndExitProcess()
    }

    private fun performErrorFinish(message: String) {
        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_ERROR, message)
        }

        setResult(RESULT_ERROR, resultIntent)
        finishAndExitProcess()
    }

    private fun finishAndExitProcess() {
        // TODO: check check, exit process ???
        onFinishTriggered()
//        GlobalScope.launch {
//            delay(animationDuration().toLong())
//            exitProcess(0)
//        }
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    open fun onFinishTriggered() {}

    private suspend fun saveAutoSave(game: Game) {
        if (!isAutoSaveEnabled()) return
        val state = getCurrentSaveState()

        if (state != null) {
            mInstances.mStatesManager.setAutoSave(game, systemCoreConfig.coreID, state)
            Timber.i("Stored autosave file with size: ${state.state.size}")
        }
    }

    private suspend fun saveSRAM(game: Game) {
        val retroGameView = retroGameView ?: return
        val sramState = retroGameView.serializeSRAM()
        mInstances.mSaverManager.setSaveRAM(game, sramState)
        Timber.i("Stored SRAM file with size: ${sramState.size}")
    }

    private suspend fun saveSlot(index: Int, name: String, time: Long) {
        if (loadingState.value) return

        withLoading {
            mInstances.mStatesHelper.saveTitleAndDate(
                game, systemCoreConfig.coreID, index, name, time
            )

            getCurrentSaveState()?.let {
                mInstances.mStatesManager.setSlotSave(game, it, systemCoreConfig.coreID, index)
                runCatching {
                    takeScreenshotPreview(index)
                }
            }
        }

        if (isFinishing || isDestroyed) return

        runOnUiThread {
            Toast.makeText(this@BaseGameActivityNovoSyx, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadSlot(index: Int) {
        if (loadingState.value) return
        withLoading {
            try {
                mInstances.mStatesManager.getSlotSave(game, systemCoreConfig.coreID, index)?.let {
                    val loaded = withContext(Dispatchers.IO) {
                        loadSaveState(it)
                    }
                    withContext(Dispatchers.Main) {
                        if (!loaded) displayToast("Cannot load this state")
                    }
                }
            } catch (e: Throwable) {
                displayLoadStateErrorMessage(e)
            }
        }
    }

    private fun getCurrentSaveState(): SaveState? {
        val retroGameView = retroGameView ?: return null
        val currentDisk = if (system.hasMultiDiskSupport) {
            retroGameView.getCurrentDisk()
        } else {
            0
        }
        return SaveState(
            retroGameView.serializeState(),
            SaveState.Metadata(currentDisk, systemCoreConfig.statesVersion)
        )
    }

    private fun loadSaveState(saveState: SaveState): Boolean {
        val retroGameView = retroGameView ?: return false

        if (systemCoreConfig.statesVersion != saveState.metadata.version) {
            throw IncompatibleStateException()
        }

        if (system.hasMultiDiskSupport &&
            retroGameView.getAvailableDisks() > 1 &&
            retroGameView.getCurrentDisk() != saveState.metadata.diskIndex
        ) {
            retroGameView.changeDisk(saveState.metadata.diskIndex)
        }

        return retroGameView.unserializeState(saveState.state)
    }

    private suspend fun displayLoadStateErrorMessage(throwable: Throwable) = withContext(Dispatchers.Main) {
        when (throwable) {
            is IncompatibleStateException ->
                displayToast(
                    "This save state is not compatible with the current core version. Please load the latest in-game save or use a previous Emulair version.",
                    Toast.LENGTH_LONG
                )

            else -> displayToast("Cannot load this state")
        }
    }

    private suspend fun reset() = withLoading {
        try {
            delay(longAnimationDuration().toLong())
            retroGameViewFlow().reset()
        } catch (e: Throwable) {
            Timber.e(e, "Error in reset")
        }
    }

    private suspend fun loadGame() {
        val requestLoadSave = intent.getBooleanExtra(EXTRA_LOAD_SAVE, false)

        val autoSaveEnabled = mInstances.mSettingManager.autoSave()
        val filter = mInstances.mSettingManager.screenFilter()
        val lowLatencyAudio = mInstances.mSettingManager.lowLatencyAudio()
        val enableRumble = mInstances.mSettingManager.enableRumble()
        val directLoad = mInstances.mSettingManager.allowDirectGameLoad()

        val loadingStatesFlow = mInstances.mGameLoader.load(
            applicationContext,
            game,
            requestLoadSave && autoSaveEnabled,
            systemCoreConfig,
            directLoad
        )

        loadingStatesFlow
            .flowOn(Dispatchers.IO)
            .catch {
                displayGameLoaderError((it as GameLoaderException).error, systemCoreConfig)
            }
            .collect { loadingState ->
                displayLoadingState(loadingState)
                if (loadingState is GameLoader.LoadingState.Ready) {
                    retroGameView = initializeRetroGameView(
                        loadingState.gameData,
                        filter,
                        lowLatencyAudio,
                        systemCoreConfig.rumbleSupported && enableRumble
                    )
                }
            }
    }

    private suspend fun initializeLoadingViewFlow() {
        withLoading {
            waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
        }
    }

    private suspend fun retroGameViewFlow() = retroGameViewFlow.filterNotNull().first()

    private fun displayLoadingState(loadingState: GameLoader.LoadingState) {
        loadingMessageStateFlow.value = when (loadingState) {
            is GameLoader.LoadingState.LoadingCore -> R.string.core_loading
            is GameLoader.LoadingState.LoadingGame -> R.string.game_preparing
            else -> R.string.please_wait
        }
    }

    private fun displayGameLoaderError(gameError: GameLoaderError, coreConfig: SystemCoreConfig) {
        val messageId = when (gameError) {
            is GameLoaderError.GLIncompatible -> "This Core requires an OpenGL ES version which is not supported by your device."
            is GameLoaderError.Generic -> "Something bad happened. Try the following:\\n\\n" +
                    "        \\n\\u2022 Disable \"Save state on correct quit\" and re-run the game\\n" +
                    "        \\n\\u2022 Go into Android settings and clear the app cache\\n" +
                    "        \\n\\u2022 Go into Emulair settings and perform a Factory Reset\\n" +
                    "        \\n\\n If none of the above works, it might be possible that this game/core is not supported by this device."
            is GameLoaderError.LoadCore -> "Core Loading -> Failed"
            is GameLoaderError.LoadGame -> "Something bad happened while loading your game. Make sure the ROM is supported by Emulair and rescan your games."
            is GameLoaderError.Saves -> "Something bad happened with your save file. If the issue persists, try disabling auto-save."
            is GameLoaderError.UnsupportedArchitecture -> "This Core is not supported by your device."
            is GameLoaderError.MissingBiosFiles -> String.format(
                "It looks like you\'re missing the required BIOS, put them in your ROMs directory and rescan. Required files: %s",
                gameError.missingFiles
            )
        }

        performErrorFinish(messageId)
    }

    companion object {
        private const val EXTRA_GAME = "GAME"
        private const val EXTRA_LOAD_SAVE = "LOAD_SAVE"
        private const val EXTRA_SYSTEM_CORE_CONFIG = "EXTRA_SYSTEM_CORE_CONFIG"

        const val REQUEST_PLAY_GAME = 1001
        const val PLAY_GAME_RESULT_SESSION_DURATION = "PLAY_GAME_RESULT_SESSION_DURATION"
        const val PLAY_GAME_RESULT_GAME = "PLAY_GAME_RESULT_GAME"
        const val PLAY_GAME_RESULT_ERROR = "PLAY_GAME_RESULT_ERROR"

        const val RESULT_ERROR = Activity.RESULT_FIRST_USER + 2
        const val RESULT_UNEXPECTED_ERROR = Activity.RESULT_FIRST_USER + 3

        fun launchGame(
            mContext: Context,
            systemCoreConfig: SystemCoreConfig,
            game: Game,
            loadSave: Boolean
        ): Intent {
            return Intent(mContext, GameActivityNovoSyx::class.java).apply {
                putExtra(EXTRA_GAME, game)
                putExtra(EXTRA_LOAD_SAVE, loadSave)
                putExtra(EXTRA_SYSTEM_CORE_CONFIG, systemCoreConfig)
            }
        }
    }

    protected abstract val mGameMenu: GameMenu
    protected abstract val gameContainerLayout: FrameLayout

    abstract fun showLLoading(isShow: Boolean)
    abstract fun setTxtLoading(txt: Int)

    @CallSuper
    open fun handleAction(action: IGameMenuAction): Boolean {
        return when(action) {
            is RestartAction -> {
                GlobalScope.launch { reset() }
                true
            }
            is QuitAction -> {
                GlobalScope.launch { autoSaveAndFinish() }
                true
            }
            is MuteAction -> {
                retroGameView?.audioEnabled = !action.isEnable
                retroGameView != null
            }
            is ForwardAction -> {
                retroGameView?.frameSpeed = if (action.isEnable) 2 else 1
                retroGameView != null
            }
            is SaveStateAction -> {
                GlobalScope.launch { saveSlot(action.iSlot, action.name, action.time) }
                true
            }
            is LoadStateAction -> {
                GlobalScope.launch { loadSlot(action.iSlot) }
                true
            }
            else -> false
        }
    }

    protected fun getInfosToCoreSettings(): Triple<List<EmulairCoreOptionNovoSyx>, List<EmulairCoreOptionNovoSyx>, Game> {
        val coreOptions = getCoreOptions()

        val options = systemCoreConfig.exposedSettings
            .mapNotNull { transformExposedSetting(it, coreOptions) }

        val advancedOptions = systemCoreConfig.exposedAdvancedSettings
            .mapNotNull { transformExposedSetting(it, coreOptions) }

        return Triple(options, advancedOptions, game)
    }

    protected fun getInfosToSave(): Pair<Game, SystemCoreConfig> = game to systemCoreConfig
}
