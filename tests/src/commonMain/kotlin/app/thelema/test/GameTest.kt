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

import app.thelema.app.APP
import app.thelema.audio.AL
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.fs.FS
import app.thelema.g3d.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.light.PointLight
import app.thelema.g3d.light.directionalLight
import app.thelema.gltf.GLTFConf
import app.thelema.gltf.gltf
import app.thelema.img.TextureCube
import app.thelema.input.KB
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.shader.ForwardRenderingPipeline
import app.thelema.ui.InputEvent
import app.thelema.ui.InputListener
import app.thelema.ui.HeadUpDisplay

class GameTest: Test {
    override fun testMain() {
        ActiveCamera {
            enablePreviousMatrix()
            near = 0.1f
            far = 1000f
            fov = 50f
        }

        val skyboxTexture = TextureCube("space-skybox.png", TextureCube.defaultSkyboxLayout())

        val baker = IBLMapBaker()
        baker.generateBrdfLUT()
        baker.environment = skyboxTexture
        baker.render()

        val pipeline = ForwardRenderingPipeline()
        pipeline.setResolution(APP.width, APP.height)

        val mainScene = Entity {
            makeCurrent()

            scene {
                renderingPipeline = pipeline
                world = World().apply {
                    ambientColor.set(0f, 0.0005f, 0.001f)
                    environmentIrradianceMap = baker.irradianceMap
                    environmentPrefilterMap = baker.prefilterMap
                    brdfLUTMap = baker.brdfLUT
                }
            }
            skybox {
                setupVelocityShader()
                textureNode.texture = skyboxTexture
                shader.build()
            }
            entity("sun") {
                directionalLight {
                    setDirectionFromPosition(1f, 1f, 1f)
                    color.set(1f, 0.8f, 0.5f, 1f)
                    intensity = 5f
                    setupShadowMaps(2048, 2048)
                    lightPositionOffset = 100f
                }
            }
        }

        val player = mainScene.entity("atrocita") {
            component<PointLight> {
                color.set(0f, 0.2f, 1f, 1f)
                range = 2f
                intensity = 1f
            }
        }

        val playerNode = player.transformNode()

        CameraControl.objectNode = playerNode
        playerNode.position.y = 1f
        CameraControl.maxTargetDistance = 7f
        CameraControl.targetOffset.y = 0.5f

        AL.newMusic(FS.internal("deepengine.ogg")).apply {
            isLooping = true
            volume = 2f
            play()
        }

        RES.gltf("atrocita/atrocita.gltf") {
            conf = GLTFConf {
//                separateThread = true
                setupVelocityShader = true
                receiveShadows = true
                setupDepthRendering = true
                ibl = true
                iblMaxMipLevels = baker.maxMipLevels
                pbrConf = {
                    shadowCascadesNum = 5
                }
            }

            onLoaded {
                scene.copyDeep(player)
            }
        }

        RES.gltf("the_moon/scene.gltf") {
            conf = GLTFConf {
                //separateThread = true
                setupVelocityShader = true
                setupDepthRendering = true
                receiveShadows = false
                ibl = true
                iblMaxMipLevels = baker.maxMipLevels
                pbrConf = {
                    shadowCascadesNum = 5
                }
            }

            onLoaded {
                mainScene.addEntity(scene.copyDeep("moon").apply {
                    transformNode {
                        scale.set(100f, 100f, 100f)
                        requestTransformUpdate()
                    }
                })
            }
        }

        val controlMode = 1

        val tmpMat = Mat4()
        val tmpVec = Vec3()
        val direction = Vec3(0f, 0f, 1f)

        val stage = HeadUpDisplay()
        MOUSE.addListener(stage)
        KB.addListener(stage)
        stage.addListener(CameraControl)
        stage.addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ESCAPE -> {
                        CameraControl.isEnabled = !CameraControl.isEnabled
                        MOUSE.isCursorEnabled = !CameraControl.isEnabled
                        CameraControl.updateCursorPosition(stage)
                    }
                }
                return super.keyDown(event, keycode)
            }
        })

        CameraControl.isEnabled = false

        APP.onUpdate = { delta ->
            when (controlMode) {
                0 -> {
                    val currentAngle = playerNode.rotation.getQuaternionAngleAround(0f, 1f, 0f)
                    val angleSpeed = 10f * delta
                    val angle: Float

                    val desiredAngle = CameraControl.forwardAngle

                    val wIsDown = KEY.isPressed(KEY.W)
                    val ctrlIsDown = KEY.isPressed(KEY.CONTROL_LEFT)
                    val spaceIsDown = KEY.isPressed(KEY.SPACE)
                    val aIsDown = KEY.isPressed(KEY.A)
                    val sIsDown = KEY.isPressed(KEY.S)
                    val dIsDown = KEY.isPressed(KEY.D)
                    val shiftDown = KEY.shiftPressed

                    angle = if (wIsDown || aIsDown || sIsDown || dIsDown) {
                        MATH.lerpAngle(currentAngle, desiredAngle, MATH.clamp(angleSpeed, 0f, 1f))
                    } else {
                        currentAngle
                    }

                    val moveAngle = angle + (when {
                        wIsDown && aIsDown -> 45f
                        wIsDown && dIsDown -> -45f
                        sIsDown && aIsDown -> 135f
                        sIsDown && dIsDown -> -135f
                        aIsDown -> 90f
                        dIsDown -> -90f
                        sIsDown -> 180f
                        else -> 0f
                    } * MATH.degRad)

                    var speed = 0f

                    val walkSpeed = delta * 10.5f
                    val runSpeed = walkSpeed * 3.3f
                    if (wIsDown || aIsDown || dIsDown || sIsDown) {
                        speed = if (shiftDown) walkSpeed else runSpeed
                    }

                    var yMoving = 0f
                    if (spaceIsDown) {
                        yMoving = if (shiftDown) walkSpeed else runSpeed
                    } else if (ctrlIsDown) {
                        yMoving = if (shiftDown) -walkSpeed else -runSpeed
                    }

                    playerNode.position.add(MATH.sin(moveAngle) * speed, yMoving, MATH.cos(moveAngle) * speed)

                    playerNode.rotation.setQuaternionByAxis(MATH.Y, angle)
                }
                1 -> {
                    val currentAngle = playerNode.rotation.getQuaternionAngleAround(0f, 1f, 0f)
                    val angleSpeed = 10f * delta
                    val angle: Float

                    val desiredAngle = CameraControl.forwardAngle

                    val wIsDown = KEY.isPressed(KEY.W)
                    val ctrlIsDown = KEY.isPressed(KEY.CONTROL_LEFT)
                    val spaceIsDown = KEY.isPressed(KEY.SPACE)
                    val aIsDown = KEY.isPressed(KEY.A)
                    val sIsDown = KEY.isPressed(KEY.S)
                    val dIsDown = KEY.isPressed(KEY.D)
                    val shiftDown = KEY.shiftPressed

                    var speed = 0f

                    val walkSpeed = delta * 10.5f
                    val runSpeed = walkSpeed * 3.3f
                    if (wIsDown || aIsDown || dIsDown || sIsDown || spaceIsDown || ctrlIsDown) {
                        speed = if (shiftDown) walkSpeed else runSpeed

                        tmpMat.set(ActiveCamera.viewMatrix)
                        tmpMat.tra()
                        tmpMat.rotate(MATH.Y, MATH.PI)
                        tmpMat.getRotation(playerNode.rotation)

                        ActiveCamera.viewMatrix.getRow2Vec3(direction)

                        if (wIsDown) {
                            direction.scl(-speed)
                            playerNode.position.add(direction)
                        } else if (sIsDown) {
                            direction.scl(speed)
                            playerNode.position.add(direction)
                        }
                    }

                    if (spaceIsDown) {
                        ActiveCamera.viewMatrix.getRow1Vec3(tmpVec).scl(speed)
                        playerNode.position.add(tmpVec)
                    } else if (ctrlIsDown) {
                        ActiveCamera.viewMatrix.getRow1Vec3(tmpVec).scl(-speed)
                        playerNode.position.add(tmpVec)
                    }

                    if (dIsDown) {
                        ActiveCamera.viewMatrix.getRow0Vec3(tmpVec).scl(speed)
                        playerNode.position.add(tmpVec)
                    } else if (aIsDown) {
                        ActiveCamera.viewMatrix.getRow0Vec3(tmpVec).scl(-speed)
                        playerNode.position.add(tmpVec)
                    }

                    //playerNode.rotation.setQuaternionByAxis(MATH.Y, angle)
                }
            }

            CameraControl.update(delta)

            playerNode.requestTransformUpdate()

            stage.update(delta)

            ECS.update(delta)
        }

        APP.onRender = {
            ECS.render()
            stage.render()
        }
    }
}
