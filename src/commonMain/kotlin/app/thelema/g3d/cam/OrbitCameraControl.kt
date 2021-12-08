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

package app.thelema.g3d.cam

import app.thelema.app.APP
import app.thelema.ecs.*
import app.thelema.input.*
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Orbit control uses spherical coordinates [zenith], [azimuth] and [targetDistance].
 *
 * See [Spherical coordinate system](https://en.wikipedia.org/wiki/Spherical_coordinate_system)
 *
 * @author zeganstyl */
class OrbitCameraControl(): IEntityComponent {
    constructor(block: OrbitCameraControl.() -> Unit): this() {
        block(this)
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.mainLoopOnUpdate { update(it) }
        }

    override val componentName: String
        get() = "OrbitCameraControl"

    /** Zenith (vertical) angle in radians */
    var zenith: Float = 1f
    /** Azimuth (horizontal) angle in radians */
    var azimuth: Float = 0f

    var currentZenith: Float = zenith
    var currentAzimuth: Float = azimuth
    var smoothRotation: Boolean = true
    var smoothRotationTransition: Float = 0.25f

    var maxZenith: Float = 3.14f
    var minZenith: Float = 0.01f

    var scrollFactor: Float = 0.1f
    var rotationSpeed: Float = 8f
    var translationSpeed: Float = 2f
    var scrollTransition: Float = 0.3f
    var translationTransition: Float = 1f

    var camera: ICamera = ActiveCamera

    var isEnabled: Boolean = true

    var target: IVec3 = Vec3()

    protected var currentTargetDistance = 5f

    protected var startX: Float = 0f
    protected var startY: Float = 0f

    protected val desiredPosNormalized = Vec3()
    protected val camPos = Vec3()

    protected var pressedButton = 0

    val right = Vec3()
    val up = Vec3()

    var isTranslationEnabled: Boolean = true

    /** If equals -1, then any button will have rotate effect */
    var rotateButton: Int = BUTTON.LEFT

    var translateButton: Int = BUTTON.MIDDLE

    var maxTargetDistance: Float = -1f
    var minTargetDistance: Float = -1f

    var targetDistance: Float = 5f
        set(value) {
            field = value
            if (maxTargetDistance > 0f && maxTargetDistance > minTargetDistance) {
                field = min(field, maxTargetDistance)
            }
            if (minTargetDistance > 0f && minTargetDistance < maxTargetDistance) {
                field = max(field, minTargetDistance)
            }
        }

    val mouseListener = object : IMouseListener {
        override fun buttonDown(button: Int, x: Int, y: Int, pointer: Int) {
            buttonDown(x.toFloat(), y.toFloat(), button)
        }

        override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
            dragged(screenX.toFloat(), screenY.toFloat())
        }

        override fun scrolled(amount: Int) {
            scrolled(MOUSE.x.toFloat(), MOUSE.y.toFloat(), amount)
        }
    }

    val touchListener = object : ITouchListener {
        override fun touchDown(x: Float, y: Float, pointer: Int, button: Int) {
            buttonDown(x, y, button)
        }

        override fun dragged(x: Float, y: Float, pointer: Int) {
            dragged(x, y)
        }

        override fun moved(x: Float, y: Float, pointer: Int) {
            dragged(x, y)
        }

        override fun scale(factor: Float, focusX: Float, focusY: Float) {
            if (isEnabled && mouseEnabled) {
                targetDistance *= scrollFactor * 10f / factor
            }
        }
    }

    var mouseEnabled = true
    var keyboardEnabled = true

    init {
        desiredPosNormalized.x = sin(zenith) * cos(azimuth)
        desiredPosNormalized.y = cos(zenith)
        desiredPosNormalized.z = sin(zenith) * sin(azimuth)
        listenToMouse()
    }

    /** Will create mouse listener and will listen to mouse */
    fun listenToMouse(): IMouseListener {
        if (APP.isMobile) {
            TOUCH.addListener(touchListener)
        } else {
            MOUSE.addListener(mouseListener)
        }
        return mouseListener
    }

    fun stopListenMouse() {
        TOUCH.removeListener(touchListener)
        MOUSE.removeListener(mouseListener)
    }

    fun update(delta: Float = APP.deltaTime) {
        if (isEnabled) {
            val delta2 = 60f * delta

            if (keyboardEnabled) keyboardUpdate()

            currentTargetDistance += (targetDistance - currentTargetDistance) * scrollTransition

            if (zenith != currentZenith || azimuth != currentAzimuth) {
                if (smoothRotation) {
                    currentZenith += (zenith - currentZenith) * smoothRotationTransition
                    currentAzimuth += (azimuth - currentAzimuth) * smoothRotationTransition
                } else {
                    currentZenith = zenith
                    currentAzimuth = azimuth
                }

                desiredPosNormalized.x = sin(currentZenith) * cos(currentAzimuth)
                desiredPosNormalized.y = cos(currentZenith)
                desiredPosNormalized.z = sin(currentZenith) * sin(currentAzimuth)
            }

            camPos.set(desiredPosNormalized).scl(currentTargetDistance).add(target)

            camera.position.lerp(camPos, min(translationTransition * delta2, 1f))

            camera.lookAt(camera.position, target)

            camera.viewMatrix.getRow0Vec3(right)
            camera.viewMatrix.getRow1Vec3(up)

            camera.node.requestTransformUpdate()
            camera.node.updateTransform() // FIXME
            camera.requestCameraUpdate()
        }
    }

    fun updateNow(delta: Float = APP.deltaTime) {
        update(delta)
        camera.updateCamera()
    }

    private fun keyboardUpdate() {
        var dRight = if (KEY.isPressed(KEY.A)) -1f else if (KEY.isPressed(KEY.D)) 1f else 0f
        var dUp = if (KEY.isPressed(KEY.E)) 1f else if (KEY.isPressed(KEY.Q)) -1f else 0f
        val dForward = if (KEY.isPressed(KEY.W)) -1f else if (KEY.isPressed(KEY.S)) 1f else 0f
        val dAzimuth = if (KEY.isPressed(KEY.J)) 1f else if (KEY.isPressed(KEY.L)) -1f else 0f
        val dZenith = if (KEY.isPressed(KEY.I)) -1f else if (KEY.isPressed(KEY.K)) 1f else 0f

        if (dRight != 0f) {
            dRight *= translationSpeed * 0.01f * currentTargetDistance
            target.add(right.x * dRight, right.y * dRight, right.z * dRight)
        }

        if (dUp != 0f) {
            dUp *= translationSpeed * 0.01f * currentTargetDistance
            target.add(up.x * dUp, up.y * dUp, up.z * dUp)
        }

        if (dForward != 0f) {
            targetDistance += dForward * translationSpeed * 0.01f * currentTargetDistance
        }

        updateAzimuthZenith(azimuth + dAzimuth * rotationSpeed * 0.005f, zenith + dZenith * rotationSpeed * 0.005f)
    }

    fun updateAzimuthZenith(a: Float, z: Float) {
        azimuth = a
        zenith = z

        if (z < minZenith) {
            this.zenith = minZenith
        }
        if (z > maxZenith) {
            this.zenith = maxZenith
        }
    }

    fun buttonDown(x: Float, y: Float, button: Int) {
        if (isEnabled && mouseEnabled) {
            pressedButton = button
            startX = x
            startY = y
        }
    }

    fun dragged(x: Float, y: Float) {
        if (isEnabled && mouseEnabled) {
            if (APP.isDesktop) {
                if ((pressedButton == rotateButton || rotateButton == -1) && (translateButton != rotateButton || !KEY.shiftPressed)) {
                    // rotate
                    val dAzimuth = rotationSpeed * (x - startX) / APP.width
                    val dZenith = -rotationSpeed * (y - startY) / APP.height
                    updateAzimuthZenith(azimuth + dAzimuth, zenith + dZenith)

                } else if (pressedButton == translateButton && isTranslationEnabled && (translateButton != rotateButton || KEY.shiftPressed)) {
                    // translate
                    val deltaX = -translationSpeed * currentTargetDistance * (x - startX) / APP.width
                    val deltaY = translationSpeed * currentTargetDistance * (y - startY) / APP.height

                    target.add(up.x * deltaY, up.y * deltaY, up.z * deltaY)
                    target.add(right.x * deltaX, right.y * deltaX, right.z * deltaX)
                }
            } else {
                val dAzimuth = rotationSpeed * (x - startX) / APP.width
                val dZenith = -rotationSpeed * (y - startY) / APP.height
                updateAzimuthZenith(azimuth + dAzimuth, zenith + dZenith)
            }

            startX = x
            startY = y
        }
    }

    fun scrolled(x: Float, y: Float, amount: Int) {
        if (isEnabled && mouseEnabled) {
            targetDistance *= 1f + amount * scrollFactor
        }
    }
}

fun IEntity.orbitCameraControl(block: OrbitCameraControl.() -> Unit) = component(block)
fun IEntity.orbitCameraControl() = component<OrbitCameraControl>()
