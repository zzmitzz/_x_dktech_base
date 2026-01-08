package com.emulator.retro.console.game.retro_game.manager.tilt

import com.swordfish.radialgamepad.library.RadialGamePad

class StickTiltTrackerNovoSyx(val id: Int) : TiltTrackerNovoSyx {

    override fun updateTracking(xTilt: Float, yTilt: Float, pads: Sequence<RadialGamePad>) {
        pads.forEach { it.simulateMotionEvent(id, xTilt, yTilt) }
    }

    override fun stopTracking(pads: Sequence<RadialGamePad>) {
        pads.forEach { it.simulateClearMotionEvent(id) }
    }

    override fun trackedIds(): Set<Int> = setOf(id)
}
