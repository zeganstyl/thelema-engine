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

package app.thelema.test.g3d.gltf

import app.thelema.app.APP
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gltf.GLTF
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.test.Test

class GLTFLoadMultithreaded: Test {
    override val name: String
        get() = "glTF load multithreaded"

    override fun testMain() {
        val mainScene = Scene()

        ActiveCamera {
            lookAt(Vec3(1f, 1f, 1f), Vec3(0f, 0f, 0f))
            near = 0.01f
            far = 1000f
            updateCamera()
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        RES.loadTyped<GLTF>(
            uri = "gearbox.glb",
            block = {
                conf.separateThread = true
                conf.setupVelocityShader = false
                conf.mergeVertexAttributes = false

                onLoaded {
                    val newScene = scene!!.entity.copyDeep()
                    newScene.name = "newScene"
                    mainScene.entity.addEntity(newScene)
                }
            }
        )

        RES.loadTyped<GLTF>(
            uri = "ToyCar.glb",
            block = {
                conf.separateThread = true
                conf.setupVelocityShader = false
                conf.mergeVertexAttributes = false

                onLoaded {
                    val newScene = scene!!.entity.copyDeep()
                    newScene.name = "newScene2"
                    mainScene.entity.addEntity(newScene)
                }
            }
        )

        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            mainScene.entity.update(APP.deltaTime)
            mainScene.render()
        }
    }
}