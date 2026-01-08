package com.emulator.retro.console.game.retro_game.manager.tilt

import com.swordfish.radialgamepad.library.RadialGamePad

interface TiltTrackerNovoSyx {

    fun updateTracking(xTilt: Float, yTilt: Float, pads: Sequence<RadialGamePad>)

    fun stopTracking(pads: Sequence<RadialGamePad>)

    fun trackedIds(): Set<Int>
}
