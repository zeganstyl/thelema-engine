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
import app.thelema.ecs.ECS
import app.thelema.ecs.component
import app.thelema.g3d.IScene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gltf.GLTF
import app.thelema.res.RES
import app.thelema.test.Test

/** @author zeganstyl */
class GLTFLoaderAnimationTest: Test {
    override val name: String
        get() = "glTF Loader Animation"

    override fun testMain() {
        var currentScene: IScene? = null

        val control = OrbitCameraControl()
        control.listenToMouse()

        RES.loadTyped<GLTF>(
            uri = "andriod_yellow_2.glb",
            block = {
                conf.separateThread = false
                conf.setupVelocityShader = false
                conf.mergeVertexAttributes = false

                onLoaded {
                    currentScene = scene!!.entity.copyDeep().component()
                }
            }
        )

        GL.isDepthTestEnabled = true
        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            currentScene?.entityOrNull?.update(APP.deltaTime)
            currentScene?.render()
        }
    }
}