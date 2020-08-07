/*
 * Copyright 2020 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ksdfv.thelema.g3d.cam

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.input.IMouseListener
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Orbit control uses spherical coordinates [zenith], [azimuth] and [targetDistance].
 *
 * See [Spherical coordinate system](https://en.wikipedia.org/wiki/Spherical_coordinate_system)
 *
 * @author zeganstyl */
open class OrbitCameraControl(
    /** Zenith (vertical) angle in radians */
    var zenith: Float = 1f,
    /** Azimuth (horizontal) angle in radians */
    var azimuth: Float = 0f,

    var maxZenith: Float = 3.141f,
    var minZenith: Float = 0.001f,

    var targetDistance: Float = 5f,

    var scrollFactor: Float = 0.1f,
    var angularSpeed: Float = 8f,
    var translationSpeed: Float = 2f,
    var scrollTransition: Float = 0.3f,
    var translationTransition: Float = 1f,

    var camera: ICamera = ActiveCamera,

    var isEnabled: Boolean = true,

    val target: IVec3 = Vec3()
) {
    protected var currentTargetDistance = 5f

    protected var startX: Float = 0f
    protected var startY: Float = 0f

    protected val desiredPosNormalized = Vec3()
    protected val camPos = Vec3()

    protected var pressedButton = 0

    val right = Vec3()
    val up = Vec3()

    val help: String
        get() = "Use middle mouse button, shift key to rotate, move camera"

    init {
        desiredPosNormalized.x = sin(zenith) * cos(azimuth)
        desiredPosNormalized.y = cos(zenith)
        desiredPosNormalized.z = sin(zenith) * sin(azimuth)
    }

    /** Will create mouse listener and will listen to mouse */
    fun listenToMouse(): IMouseListener {
        val listener = object : IMouseListener {
            override fun buttonDown(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                buttonDown(screenX.toFloat(), screenY.toFloat(), button)
            }

            override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
                dragged(screenX.toFloat(), screenY.toFloat())
            }

            override fun scrolled(amount: Int) {
                scrolled(MOUSE.x.toFloat(), MOUSE.y.toFloat(), amount)
            }
        }

        MOUSE.addListener(listener)

        return listener
    }

    open fun update(delta: Float) {
        if (isEnabled) {
            val delta2 = 60f * delta

            currentTargetDistance += (targetDistance - currentTargetDistance) * scrollTransition

            camPos.set(desiredPosNormalized).scl(currentTargetDistance).add(target)

            camera.position.lerp(camPos, min(translationTransition * delta2, 1f))

            camera.lookAt(camera.position, target)

            camera.viewMatrix.getRow3(IMat4.Right, right)
            camera.viewMatrix.getRow3(IMat4.Up, up)
        }
    }

    open fun buttonDown(x: Float, y: Float, button: Int) {
        if (isEnabled) {
            pressedButton = button
            if (button == MOUSE.MIDDLE) {
                startX = x
                startY = y
            }
        }
    }

    open fun dragged(x: Float, y: Float) {
        if (isEnabled) {
            if (pressedButton == MOUSE.MIDDLE) {
                if (KB.shift) {
                    // translate

                    val deltaX = -translationSpeed * currentTargetDistance * (x - startX) / APP.width
                    val deltaY = translationSpeed * currentTargetDistance * (y - startY) / APP.width

                    target.add(up.x * deltaY, up.y * deltaY, up.z * deltaY)
                    target.add(right.x * deltaX, right.y * deltaX, right.z * deltaX)
                } else {
                    // rotate
                    val dAzimuth = angularSpeed * (x - startX) / APP.width
                    val dZenith = -angularSpeed * (y - startY) / APP.height

                    azimuth += dAzimuth
                    zenith += dZenith

                    if (zenith < minZenith) {
                        zenith = minZenith
                    }
                    if (zenith > maxZenith) {
                        zenith = maxZenith
                    }

                    desiredPosNormalized.x = sin(zenith) * cos(azimuth)
                    desiredPosNormalized.y = cos(zenith)
                    desiredPosNormalized.z = sin(zenith) * sin(azimuth)
                }

                startX = x
                startY = y
            }
        }
    }

    open fun scrolled(x: Float, y: Float, amount: Int) {
        if (isEnabled) {
            targetDistance *= 1f + amount * scrollFactor
        }
    }
}