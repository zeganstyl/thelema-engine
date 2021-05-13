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

package app.thelema.test.gl

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.img.Texture2D
import app.thelema.math.Mat4
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class AlphaBlendingTest: Test {
    override val name: String
        get() = "Alpha blending test"

    override fun testMain() {
        val box = BoxMeshBuilder().build()

        val color = Vec4(1f)
        color.a = 0.5f

        val matrix = Mat4()

        val shader = SimpleShader3D {
            this.color = color
            multiplyColor = true
            worldMatrix = matrix
            colorTexture = Texture2D().load("thelema-logo-128.png")
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.setSimpleAlphaBlending()
        GL.isBlendingEnabled = true
        GL.isDepthTestEnabled = false
        GL.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        APP.onRender = {
            GL.glClear()

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            val alpha = 0.5f
            val colorMul = 0f

            color.set(1f, colorMul, colorMul, alpha)
            matrix.setToTranslation(-1.5f, 0f, 0f)
            shader.render(box)

            color.set(colorMul, 1f, colorMul, alpha)
            matrix.setToTranslation(0f, 0f, 0f)
            shader.render(box)

            color.set(colorMul, colorMul, 1f, alpha)
            matrix.setToTranslation(1.5f, 0f, 0f)
            shader.render(box)
        }
    }
}
