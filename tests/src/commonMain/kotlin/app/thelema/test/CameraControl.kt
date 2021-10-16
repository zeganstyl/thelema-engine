/*
 * Copyright 2020-2021 Anton Trushkov
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

package app.thelema.test

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.ICamera
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.input.MOUSE
import app.thelema.math.MATH.atan2
import app.thelema.math.MATH.cos
import app.thelema.math.MATH.sin
import app.thelema.math.Vec2
import app.thelema.math.Vec3
import app.thelema.ui.InputEvent
import app.thelema.ui.InputListener
import app.thelema.ui.HeadUpDisplay
import kotlin.math.min

object CameraControl: InputListener {
    var desiredTargetDistance = 2f
    var minTargetDistance = 0.3f
    var maxTargetDistance = 3f

    var scrollFactor = 0.5f

    var startX: Float = 0f
    var startY: Float = 0f

    var zenith: Float = 1f
    var azimuth: Float = 0f
    var angularSpeed: Float = 5f

    val desiredPosNormalized = Vec3(sin(zenith) * cos(
        azimuth
    ), cos(zenith), sin(zenith) * sin(
        azimuth
    ))
    private val dirTmp = Vec3()
    private val desiredTargetTmp = Vec3()
    private val camPosTmp = Vec3()
    private val tmp2 = Vec2()

    var camera: ICamera = ActiveCamera

    var cameraPositionTransition = 0.3f

    var isEnabled = true

    val targetOffset = Vec3(0f, 1.8f, 0f)

    var forwardAngle = 0f

    var objectNode: ITransformNode = TransformNode()

    fun updateCursorPosition(headUpDisplay: HeadUpDisplay) {
        tmp2.set(MOUSE.x.toFloat(), MOUSE.y.toFloat())
        headUpDisplay.screenToStageCoordinates(tmp2)
        startX = tmp2.x
        startY = tmp2.y
    }

    fun update(delta: Float) {
        if (isEnabled) {
            updateNonPhysical(delta)
        }
    }

    fun updateNonPhysical(delta: Float) {
        val node = objectNode
        val delta2 = 60f * delta

        val playerPos = node.position

        dirTmp.set(desiredPosNormalized).scl(-1f)
        forwardAngle = atan2(dirTmp.x, dirTmp.z)

        desiredTargetTmp.set(playerPos).add(targetOffset)

        camPosTmp.set(desiredPosNormalized).scl(
            desiredTargetDistance
        ).add(desiredTargetTmp)

        //if (camPosTmp.y < 0f) camPosTmp.y = 0f

        camera.position.lerp(
            camPosTmp, min(
                cameraPositionTransition * delta2, 1f))
        camera.direction.lerp(dirTmp, 0.3f)

        camera.requestCameraUpdate()
    }

    override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
        if (isEnabled) {
            val dAzimuth = angularSpeed * (x - startX) / event.headUpDisplay!!.width
            val dZenith = angularSpeed * (y - startY) / event.headUpDisplay!!.height
            startX = x
            startY = y

            azimuth += dAzimuth
            zenith += dZenith

            if (zenith < 0.1f) {
                zenith = 0.1f
            }
            if (zenith > 3f) {
                zenith = 3f
            }

            desiredPosNormalized.x = sin(zenith) * cos(
                azimuth
            )
            desiredPosNormalized.y = cos(zenith)
            desiredPosNormalized.z = sin(zenith) * sin(
                azimuth
            )
        }
        return super.mouseMoved(event, x, y)
    }

//    override fun cursorEnabledChanged(oldValue: Boolean, newValue: Boolean) {
//        if (!newValue) updateCursorPosition()
//        isEnabled = !newValue
//    }

    override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
        if (isEnabled) {
            val newDesiredTargetDistance = desiredTargetDistance + amount * scrollFactor
            if (newDesiredTargetDistance in minTargetDistance..maxTargetDistance) {
                desiredTargetDistance = newDesiredTargetDistance
            }
        }
        return super.scrolled(event, x, y, amount)
    }
}