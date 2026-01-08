package com.emulator.retro.console.game.retro_game.activities

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.ads.detech.ads.AdsManager
import com.bigbratan.emulair.common.managers.controller.ControllerConfig
import com.bigbratan.emulair.common.managers.controller.EmulairTouchConfigs
import com.bigbratan.emulair.common.managers.controller.TouchControllerCustomizer
import com.bigbratan.emulair.common.managers.controller.TouchControllerSettingsManager
import com.bigbratan.emulair.common.managers.tilt.TiltSensor
import com.bigbratan.emulair.common.utils.coroutines.batchWithTime
import com.bigbratan.emulair.common.utils.coroutines.launchOnState
import com.bigbratan.emulair.common.utils.coroutines.safeCollect
import com.bigbratan.emulair.common.utils.graphics.GraphicsUtils
import com.bigbratan.emulair.common.utils.kotlin.NTuple2
import com.bigbratan.emulair.common.utils.kotlin.NTuple3
import com.bigbratan.emulair.common.utils.kotlin.allTrue
import com.bigbratan.emulair.common.utils.math.linearInterpolation
import com.emulator.retro.console.game.RemoteConfig
import com.emulator.retro.console.game.databinding.ActivityGameBinding
import com.emulator.retro.console.game.retro_game.manager.tilt.CrossTiltTrackerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.tilt.StickTiltTrackerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.tilt.TiltTrackerNovoSyx
import com.emulator.retro.console.game.retro_game.manager.tilt.TwoButtonsTiltTrackerNovoSyx
import com.emulator.retro.console.game.retro_game.menu.GameMenu
import com.emulator.retro.console.game.retro_game.utils.VirtualLongPressHandlerNovoSyx
import com.emulator.retro.console.game.utils.CommonNovoSyx
import com.emulator.retro.console.game.utils.gone
import com.emulator.retro.console.game.utils.invisible
import com.emulator.retro.console.game.utils.visible
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class GameActivityNovoSyx : BaseGameActivityNovoSyx<ActivityGameBinding>(ActivityGameBinding::inflate) {

    private val tiltSensor: TiltSensor by lazy { TiltSensor(applicationContext) }
    private var currentTiltTracker: TiltTrackerNovoSyx? = null

    private var leftPad: RadialGamePad? = null
    private var rightPad: RadialGamePad? = null

    private val touchControllerJobs = mutableSetOf<Job>()

    private val touchControllerSettingsState =
        MutableStateFlow<TouchControllerSettingsManager.Settings?>(null)
    private val insetsState = MutableStateFlow<Rect?>(null)
    private val orientationState = MutableStateFlow(Configuration.ORIENTATION_PORTRAIT)

    private fun initializeInsetsState() {
        binding.lMainContainer.setOnApplyWindowInsetsListener { _, windowInsets ->
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.displayCutout()
                )
                Rect(insets.left, insets.top, insets.right, insets.bottom)
            } else {
                Rect(0, 0, 0, 0)
            }
            insetsState.value = result
            windowInsets
        }
    }

    private fun initializeFlows() {
        launchOnState(Lifecycle.State.CREATED) {
            initializeTouchControllerFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeTiltSensitivityFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeTouchControllerVisibilityFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeTiltEventsFlow()
        }
    }

    private suspend fun initializeTouchControllerVisibilityFlow() {
        isTouchControllerVisible()
            .safeCollect {
                binding.lLeftGamepad.isVisible = it
                binding.lRightGamepad.isVisible = it
            }
    }

    private suspend fun initializeTiltEventsFlow() {
        tiltSensor
            .getTiltEvents()
            .safeCollect { sendTiltEvent(it) }
    }

    private suspend fun initializeTiltSensitivityFlow() {
        val sensitivity = mInstances.mSettingManager.tiltSensitivity()
        tiltSensor.setSensitivity(sensitivity)
    }

    private suspend fun initializeTouchControllerFlow() {
        val touchControllerFeatures = combine(getTouchControllerType(), orientationState, ::NTuple2)
            .onEach { (pad, orientation) -> setupController(pad, orientation) }

        val layoutFeatures = combine(
            isTouchControllerVisible(),
            touchControllerSettingsState.filterNotNull(),
            insetsState.filterNotNull(),
            ::NTuple3
        )

        touchControllerFeatures.combine(layoutFeatures) { e1, e2 -> e1 + e2 }
            .safeCollect { (config, orientation, touchControllerVisible, padSettings, insets) ->
                LayoutHandler().updateLayout(
                    config,
                    padSettings,
                    orientation,
                    touchControllerVisible,
                    insets
                )
            }
    }

    private fun getTouchControllerType() = getControllerType()
        .map { it[0] }
        .filterNotNull()
        .distinctUntilChanged()

    private suspend fun setupController(controllerConfig: ControllerConfig, orientation: Int) {
        val hapticFeedbackMode = mInstances.mSettingManager.hapticFeedbackMode()
        withContext(Dispatchers.Main) {
            setupTouchViews(controllerConfig, hapticFeedbackMode, orientation)
        }
        loadTouchControllerSettings(controllerConfig, orientation)
    }

    private fun isTouchControllerVisible(): Flow<Boolean> {
        return mInstances.mInputDeviceManager
            .getEnabledInputsObservable()
            .map { it.isEmpty() }
    }

    private fun getCurrentOrientation() = resources.configuration.orientation

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationState.value = newConfig.orientation
    }

    private fun setupTouchViews(
        controllerConfig: ControllerConfig,
        hapticFeedbackType: String,
        orientation: Int,
    ) {
        touchControllerJobs.forEach { it.cancel() }
        touchControllerJobs.clear()
        binding.lLeftGamepad.removeAllViews()
        binding.lRightGamepad.removeAllViews()

        val touchControllerConfig = controllerConfig.getTouchControllerConfig()

        val hapticConfig = when (hapticFeedbackType) {
            "none" -> HapticConfig.OFF
            "press" -> HapticConfig.PRESS
            "press_release" -> HapticConfig.PRESS_AND_RELEASE
            else -> HapticConfig.OFF
        }

        val leftConfig = EmulairTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.leftConfig,
            hapticConfig,
            mThemeWrapper.theme
        )
        val leftPad = RadialGamePad(leftConfig, DEFAULT_MARGINS_DP, this)
        binding.lLeftGamepad.addView(leftPad)

        val rightConfig = EmulairTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.rightConfig,
            hapticConfig,
            mThemeWrapper.theme
        )
        val rightPad = RadialGamePad(rightConfig, DEFAULT_MARGINS_DP, this)
        binding.lRightGamepad.addView(rightPad)

        val touchControllerEvents = merge(leftPad.events(), rightPad.events())
            .shareIn(lifecycleScope, SharingStarted.Lazily)

        setupDefaultActions(touchControllerEvents)
        setupTiltActions(touchControllerEvents)
        setupTouchMenuActions(touchControllerEvents)

        this.leftPad = leftPad
        this.rightPad = rightPad
    }

    private fun setupDefaultActions(touchControllerEvents: Flow<Event>) {
        val job = lifecycleScope.launch {
            touchControllerEvents
                .safeCollect {
                    when (it) {
                        is Event.Button -> {
                            handleGamePadButton(it)
                        }

                        is Event.Direction -> {
                            handleGamePadDirection(it)
                        }
                    }
                }
        }
        touchControllerJobs.add(job)
    }

    private fun setupTiltActions(touchControllerEvents: Flow<Event>) {
        val job1 = lifecycleScope.launch {
            touchControllerEvents
                .filterIsInstance<Event.Gesture>()
                .filter { it.type == GestureType.TRIPLE_TAP }
                .batchWithTime(500)
                .filter { it.isNotEmpty() }
                .safeCollect { events ->
                    handleTripleTaps(events)
                }
        }

        val job2 = lifecycleScope.launch {
            touchControllerEvents
                .filterIsInstance<Event.Gesture>()
                .filter { it.type == GestureType.FIRST_TOUCH }
                .safeCollect { event ->
                    currentTiltTracker?.let { tracker ->
                        if (event.id in tracker.trackedIds()) {
                            stopTrackingId(tracker)
                        }
                    }
                }
        }

        touchControllerJobs.add(job1)
        touchControllerJobs.add(job2)
    }

    private fun setupTouchMenuActions(touchControllerEvents: Flow<Event>) {
        VirtualLongPressHandlerNovoSyx.initializeTheme(this, mThemeWrapper.theme)

        val allMenuButtonEvents = touchControllerEvents
            .filterIsInstance<Event.Button>()
            .filter { it.id == KeyEvent.KEYCODE_BUTTON_MODE }
            .shareIn(lifecycleScope, SharingStarted.Lazily)

        val cancelMenuButtonEvents = allMenuButtonEvents
            .filter { it.action == KeyEvent.ACTION_UP }
            .map { Unit }

        val job = lifecycleScope.launch {
            allMenuButtonEvents
                .filter { it.action == KeyEvent.ACTION_DOWN }
                .map {
                    VirtualLongPressHandlerNovoSyx.displayLoading(
                        this@GameActivityNovoSyx,
                        com.bigbratan.emulair.common.R.drawable.ic_menu,
                        cancelMenuButtonEvents
                    )
                }
                .filter { it }
                .safeCollect {
                    displayOptionsDialog()
                    simulateTouchControllerHaptic()
                }
        }

        touchControllerJobs.add(job)
    }

    private fun handleTripleTaps(events: List<Event.Gesture>) {
        val eventsTracker = when (events.map { it.id }.toSet()) {
            setOf(EmulairTouchConfigs.MOTION_SOURCE_LEFT_STICK) -> StickTiltTrackerNovoSyx(
                EmulairTouchConfigs.MOTION_SOURCE_LEFT_STICK
            )

            setOf(EmulairTouchConfigs.MOTION_SOURCE_RIGHT_STICK) -> StickTiltTrackerNovoSyx(
                EmulairTouchConfigs.MOTION_SOURCE_RIGHT_STICK
            )

            setOf(EmulairTouchConfigs.MOTION_SOURCE_DPAD) -> CrossTiltTrackerNovoSyx(
                EmulairTouchConfigs.MOTION_SOURCE_DPAD
            )

            setOf(EmulairTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK) -> CrossTiltTrackerNovoSyx(
                EmulairTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK
            )

            setOf(EmulairTouchConfigs.MOTION_SOURCE_RIGHT_DPAD) -> CrossTiltTrackerNovoSyx(
                EmulairTouchConfigs.MOTION_SOURCE_RIGHT_DPAD
            )

            setOf(
                KeyEvent.KEYCODE_BUTTON_L1,
                KeyEvent.KEYCODE_BUTTON_R1
            ),
                -> TwoButtonsTiltTrackerNovoSyx(
                KeyEvent.KEYCODE_BUTTON_L1,
                KeyEvent.KEYCODE_BUTTON_R1
            )

            setOf(
                KeyEvent.KEYCODE_BUTTON_L2,
                KeyEvent.KEYCODE_BUTTON_R2
            ),
                -> TwoButtonsTiltTrackerNovoSyx(
                KeyEvent.KEYCODE_BUTTON_L2,
                KeyEvent.KEYCODE_BUTTON_R2
            )

            else -> null
        }

        eventsTracker?.let { startTrackingId(eventsTracker) }
    }

    private fun handleGamePadButton(it: Event.Button) {
        retroGameView?.sendKeyEvent(it.action, it.id)
    }

    private fun handleGamePadDirection(it: Event.Direction) {
        when (it.id) {
            EmulairTouchConfigs.MOTION_SOURCE_DPAD -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }

            EmulairTouchConfigs.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
            }

            EmulairTouchConfigs.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                    it.xAxis,
                    it.yAxis
                )
            }

            EmulairTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }

            EmulairTouchConfigs.MOTION_SOURCE_RIGHT_DPAD -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                    it.xAxis,
                    it.yAxis
                )
            }
        }
    }

    private fun sendTiltEvent(sensorValues: FloatArray) {
        currentTiltTracker?.let {
            val xTilt = (sensorValues[0] + 1f) / 2f
            val yTilt = (sensorValues[1] + 1f) / 2f
            it.updateTracking(xTilt, yTilt, sequenceOf(leftPad, rightPad).filterNotNull())
        }
    }

    private fun stopTrackingId(trackedEvent: TiltTrackerNovoSyx) {
        currentTiltTracker = null
        tiltSensor.shouldRun = false
        trackedEvent.stopTracking(sequenceOf(leftPad, rightPad).filterNotNull())
    }

    private fun startTrackingId(trackedEvent: TiltTrackerNovoSyx) {
        if (currentTiltTracker != trackedEvent) {
            currentTiltTracker?.let { stopTrackingId(it) }
            currentTiltTracker = trackedEvent
            tiltSensor.shouldRun = true
            simulateTouchControllerHaptic()
        }
    }

    private fun simulateTouchControllerHaptic() {
        leftPad?.performHapticFeedback()
    }

    private suspend fun storeTouchControllerSettings(
        controllerConfig: ControllerConfig,
        orientation: Int,
        settings: TouchControllerSettingsManager.Settings,
    ) {
        val settingsManager = getTouchControllerSettingsManager(controllerConfig, orientation)
        return settingsManager.storeSettings(settings)
    }

    private suspend fun loadTouchControllerSettings(
        controllerConfig: ControllerConfig,
        orientation: Int,
    ) {
        val settingsManager = getTouchControllerSettingsManager(controllerConfig, orientation)
        touchControllerSettingsState.value = settingsManager.retrieveSettings()
    }

    private fun getTouchControllerSettingsManager(
        controllerConfig: ControllerConfig,
        orientation: Int,
    ): TouchControllerSettingsManager {
        val settingsOrientation = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            TouchControllerSettingsManager.Orientation.PORTRAIT
        } else {
            TouchControllerSettingsManager.Orientation.LANDSCAPE
        }

        return TouchControllerSettingsManager(
            applicationContext,
            controllerConfig.touchControllerID,
            mInstances.mSharedPref,
            settingsOrientation
        )
    }

    private suspend fun displayCustomizationOptions() {
        binding.vEditControlsDarkening.visible()

        val customizer = TouchControllerCustomizer()

        val insets = insetsState
            .filterNotNull()
            .first()

        val touchControllerConfig = getTouchControllerType()
            .first()

        val padSettings = touchControllerSettingsState.filterNotNull()
            .first()

        val initialSettings = TouchControllerCustomizer.Settings(
            padSettings.scale,
            padSettings.rotation,
            padSettings.marginX,
            padSettings.marginY
        )

        // todo check check
        val finalSettings = customizer.displayCustomizationPopup(
            this@GameActivityNovoSyx,
            layoutInflater,
            binding.lMainContainer,
            insets,
            initialSettings
        ).takeWhile { it !is TouchControllerCustomizer.Event.Close }
            .scan(padSettings) { current, it ->
                when (it) {
                    is TouchControllerCustomizer.Event.Scale -> {
                        current.copy(scale = it.value)
                    }

                    is TouchControllerCustomizer.Event.Rotation -> {
                        current.copy(rotation = it.value)
                    }

                    is TouchControllerCustomizer.Event.Margins -> {
                        current.copy(marginX = it.x, marginY = it.y)
                    }

                    else -> current
                }
            }
            .onEach { touchControllerSettingsState.value = it }
            .last()

        storeTouchControllerSettings(touchControllerConfig, orientationState.value, finalSettings)
        binding.vEditControlsDarkening.gone()
    }

    inner class LayoutHandler {
        fun updateLayout(
            config: ControllerConfig,
            padSettings: TouchControllerSettingsManager.Settings,
            orientation: Int,
            touchControllerVisible: Boolean,
            insets: Rect,
        ) {
            updateDividers(orientation, config, touchControllerVisible)

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.lMainContainer)

            handleTouchControllerLayout(constraintSet, padSettings, config, orientation, insets)
//            handleRetroViewLayout(constraintSet, config, orientation, touchControllerVisible)

            constraintSet.applyTo(binding.lMainContainer)

            binding.lMainContainer.setPadding(0, insets.top, 0, 0)
            binding.lMainContainer.requestLayout()
//            binding.lMainContainer.invalidate()
        }

        private fun handleRetroViewLayout(
            constraintSet: ConstraintSet,
            controllerConfig: ControllerConfig,
            orientation: Int,
            touchControllerVisible: Boolean,
        ) {
            if (!touchControllerVisible) {
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                return
            }

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.BOTTOM,
                    binding.vHorDivider.id,
                    ConstraintSet.TOP
                )

                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )

                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )

                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            } else {
                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )

                constraintSet.connect(
                    binding.cvGameContainer.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )

                if (controllerConfig.allowTouchOverlay) {
                    constraintSet.connect(
                        binding.cvGameContainer.id,
                        ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT
                    )

                    constraintSet.connect(
                        binding.cvGameContainer.id,
                        ConstraintSet.RIGHT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.RIGHT
                    )
                } else {
                    constraintSet.connect(
                        binding.cvGameContainer.id,
                        ConstraintSet.LEFT,
                        binding.vLeftVerDivider.id,
                        ConstraintSet.RIGHT
                    )

                    constraintSet.connect(
                        binding.cvGameContainer.id,
                        ConstraintSet.RIGHT,
                        binding.vRightVerDivider.id,
                        ConstraintSet.LEFT
                    )
                }
            }

            constraintSet.constrainedWidth(binding.cvGameContainer.id, true)
            constraintSet.constrainedHeight(binding.cvGameContainer.id, true)
        }

        private fun handleTouchControllerLayout(
            constraintSet: ConstraintSet,
            padSettings: TouchControllerSettingsManager.Settings,
            controllerConfig: ControllerConfig,
            orientation: Int,
            insets: Rect,
        ) {
            val mLeftPad = leftPad
            val mRightPad = rightPad
            if (mLeftPad == null || mRightPad == null) return

            val touchControllerConfig = controllerConfig.getTouchControllerConfig()

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.iv.visible()
                binding.view.visible()
                constraintSet.clear(binding.lLeftGamepad.id, ConstraintSet.TOP)
                constraintSet.clear(binding.lRightGamepad.id, ConstraintSet.TOP)
            } else {
                binding.iv.invisible()
                binding.view.invisible()
                constraintSet.connect(
                    binding.lLeftGamepad.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    binding.lRightGamepad.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            }

            val minScale = TouchControllerSettingsManager.MIN_SCALE
            val maxScale = TouchControllerSettingsManager.MAX_SCALE

            val leftScale = linearInterpolation(
                padSettings.scale,
                minScale,
                maxScale
            ) * touchControllerConfig.leftScale

            val rightScale = linearInterpolation(
                padSettings.scale,
                minScale,
                maxScale
            ) * touchControllerConfig.rightScale

            val maxMargins = GraphicsUtils.convertDpToPixel(
                TouchControllerSettingsManager.MAX_MARGINS,
                applicationContext
            )

            constraintSet.setHorizontalWeight(
                binding.lLeftGamepad.id,
                touchControllerConfig.leftScale
            )
            constraintSet.setHorizontalWeight(
                binding.lRightGamepad.id,
                touchControllerConfig.rightScale
            )

            mLeftPad.primaryDialMaxSizeDp = DEFAULT_PRIMARY_DIAL_SIZE * leftScale
            mRightPad.primaryDialMaxSizeDp = DEFAULT_PRIMARY_DIAL_SIZE * rightScale

            val baseVerticalMargin = GraphicsUtils.convertDpToPixel(
                touchControllerConfig.verticalMarginDP,
                applicationContext
            )

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupMarginsForPortrait(
                    mLeftPad,
                    mRightPad,
                    maxMargins,
                    padSettings,
                    baseVerticalMargin.roundToInt() + insets.bottom
                )
            } else {
                setupMarginsForLandscape(
                    mLeftPad,
                    mRightPad,
                    maxMargins,
                    padSettings,
                    baseVerticalMargin.roundToInt() + insets.bottom,
                    maxOf(insets.left, insets.right)
                )
            }

            mLeftPad.gravityY = 1f
            mRightPad.gravityY = 1f

            mLeftPad.gravityX = -1f
            mRightPad.gravityX = 1f

            mLeftPad.secondaryDialSpacing = 0.1f
            mRightPad.secondaryDialSpacing = 0.1f

            val constrainHeight = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ConstraintSet.WRAP_CONTENT
            } else {
                ConstraintSet.MATCH_CONSTRAINT
            }

            val constrainWidth = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ConstraintSet.MATCH_CONSTRAINT
            } else {
                ConstraintSet.WRAP_CONTENT
            }

            constraintSet.constrainHeight(binding.lLeftGamepad.id, constrainHeight)
            constraintSet.constrainHeight(binding.lRightGamepad.id, constrainHeight)
            constraintSet.constrainWidth(binding.lLeftGamepad.id, constrainWidth)
            constraintSet.constrainWidth(binding.lRightGamepad.id, constrainWidth)

            if (controllerConfig.allowTouchRotation) {
                val maxRotation = TouchControllerSettingsManager.MAX_ROTATION
                mLeftPad.secondaryDialRotation =
                    linearInterpolation(padSettings.rotation, 0f, maxRotation)
                mRightPad.secondaryDialRotation =
                    -linearInterpolation(padSettings.rotation, 0f, maxRotation)
            }
        }

        private fun setupMarginsForLandscape(
            leftPad: RadialGamePad,
            rightPad: RadialGamePad,
            maxMargins: Float,
            padSettings: TouchControllerSettingsManager.Settings,
            verticalSpacing: Int,
            horizontalSpacing: Int,
        ) {
            leftPad.spacingBottom = verticalSpacing
            leftPad.spacingLeft = linearInterpolation(
                padSettings.marginX,
                0f,
                maxMargins
            ).roundToInt() + horizontalSpacing

            rightPad.spacingBottom = verticalSpacing
            rightPad.spacingRight = linearInterpolation(
                padSettings.marginX,
                0f,
                maxMargins
            ).roundToInt() + horizontalSpacing

            leftPad.offsetX = 0f
            rightPad.offsetX = 0f

            leftPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
            rightPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
        }

        private fun setupMarginsForPortrait(
            leftPad: RadialGamePad,
            rightPad: RadialGamePad,
            maxMargins: Float,
            padSettings: TouchControllerSettingsManager.Settings,
            verticalSpacing: Int,
        ) {
            leftPad.spacingBottom = linearInterpolation(
                padSettings.marginY,
                0f,
                maxMargins
            ).roundToInt() + verticalSpacing
            leftPad.spacingLeft = 0
            rightPad.spacingBottom = linearInterpolation(
                padSettings.marginY,
                0f,
                maxMargins
            ).roundToInt() + verticalSpacing
            rightPad.spacingRight = 0

            leftPad.offsetX = linearInterpolation(padSettings.marginX, 0f, maxMargins)
            rightPad.offsetX = -linearInterpolation(padSettings.marginX, 0f, maxMargins)

            leftPad.offsetY = 0f
            rightPad.offsetY = 0f
        }

        private fun updateDividers(
            orientation: Int,
            controllerConfig: ControllerConfig,
            touchControllerVisible: Boolean,
        ) {

            val displayHorizontalDivider = allTrue(
                orientation == Configuration.ORIENTATION_PORTRAIT,
                touchControllerVisible
            )

            val displayVerticalDivider = allTrue(
                orientation != Configuration.ORIENTATION_PORTRAIT,
                !controllerConfig.allowTouchOverlay,
                touchControllerVisible
            )

            val mDividerColor = ColorStateList.valueOf(
                getColor(mThemeWrapper.dividerColor)
            )

//            updateDivider(binding.vHorDivider, displayHorizontalDivider, mDividerColor)
            updateDivider(binding.vLeftVerDivider, displayVerticalDivider, mDividerColor)
            updateDivider(binding.vRightVerDivider, displayVerticalDivider, mDividerColor)
        }

        private fun updateDivider(divider: View, visible: Boolean, color: ColorStateList) {
            divider.isVisible = visible
            divider.backgroundTintList = color
        }
    }

    companion object {
        const val DEFAULT_MARGINS_DP = 8f
        const val DEFAULT_PRIMARY_DIAL_SIZE = 160f
    }

    // TODO: CHECKME (fake pause/resume when game menu showed/hided)
    override val mGameMenu: GameMenu = GameMenu(
        this,
        getInfosToCoreSettings = ::getInfosToCoreSettings,
        getInfosToLoadOrSave = ::getInfosToSave,
        onShowed = {
            try {
                retroGameView?.onPause()
                showAds()
            } catch (_: Exception) {
            }
            tiltSensor.isAllowedToRun = false
        },
        onHided = {
            try {
                retroGameView?.onResume()
            } catch (_: Exception) {
            }
            tiltSensor.isAllowedToRun = true
        })
    override val gameContainerLayout: FrameLayout by lazy { binding.lGameContainer }

    override fun initData() {
        super.initData()
        orientationState.value = getCurrentOrientation()
    }

    override fun initView() {
        super.initView()
        mGameMenu.initView()
        applyTheme()

        initializeInsetsState()
        initializeFlows()
        CommonNovoSyx.showDialogRate(this)
    }

    override fun initActionView() {
        mGameMenu.initActionView()
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onStart() {
        super.onStart()
        showAds()
    }

    override fun onResume() {
        super.onResume()
        tiltSensor.isAllowedToRun = true

        // TODO: CHECKME (fake pause when game menu showed)
        if (mGameMenu.isShowed) {
            retroGameView?.post {
                retroGameView?.onPause()
                tiltSensor.isAllowedToRun = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tiltSensor.isAllowedToRun = false
    }

    override fun onDestroy() {
        touchControllerJobs.clear()
        super.onDestroy()
    }

    private fun applyTheme() {
        binding.vGameConsole.setBackgroundResource(mThemeWrapper.bgConsole)
        binding.ivBgConsole.setImageResource(mThemeWrapper.bgConsole1)
        if (mThemeWrapper.ivConsole != Resources.ID_NULL) {
            binding.ivGameConsole.setImageResource(mThemeWrapper.ivConsole)
        }
//        binding.cvGameContainer.setStrokeColor(
//            ColorStateList.valueOf(getColor(mThemeWrapper.borderScreenColor))
//        )
        binding.iv.setImageResource(mThemeWrapper.ivUnknown)
    }

    override fun showLLoading(isShow: Boolean) {
        binding.lLoading.isVisible = isShow
    }

    override fun setTxtLoading(txt: Int) {
        binding.tvMsgLoading.setText(txt)
    }

    private fun showAds() {
        binding.frNative.visible()
        AdsManager.showAdsBannerNative(this, RemoteConfig.ADS_PLAY, binding.frNative) {
            binding.frNative.gone()
        }
    }

}
