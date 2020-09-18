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

package org.ksdfv.thelema.test.g3d

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.g3d.gltf.GLTF
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.test.Test

/** @author zeganstyl */
class GLTFWriteTest: Test {
    override val name: String
        get() = "glTF write test"

    override fun testMain() {
        val gltf = GLTF(FS.internal("gltf/cube2/cube.gltf"))
        gltf.load {
            gltf.directory = FS.absolute("/home/q/gltf-test")
            gltf.directory.child("cube.gltf").writeText(JSON.printObject(gltf))
            gltf.saveBuffers()

            GL.glClearColor(0f, 0f, 0f, 1f)

            GL.isDepthTestEnabled = true

            ActiveCamera.proxy = Camera()

            val control = OrbitCameraControl()
            control.listenToMouse()

            GL.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                control.update(APP.deltaTime)
                ActiveCamera.update()

                gltf.scene?.render()
            }
        }
    }
}