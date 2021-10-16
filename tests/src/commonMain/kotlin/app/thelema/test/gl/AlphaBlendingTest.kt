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
import app.thelema.g3d.mesh.BoxMesh
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
        val box = BoxMesh { setSize(2f) }
        box.mesh.worldMatrix = Mat4()

        val shader = SimpleShader3D {
            setupOnlyTexture(Texture2D("thelema-logo-alpha.png"))
            alphaCutoff = 0f
        }

        val control = OrbitCameraControl()

        GL.setupSimpleAlphaBlending()
        GL.isBlendingEnabled = true
        GL.isDepthTestEnabled = false
        GL.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        APP.onRender = {
            control.updateNow()

            box.mesh.worldMatrix?.setToTranslation(-1.5f, 0f, 0f)
            shader.render(box.mesh)

            box.mesh.worldMatrix?.setToTranslation(0f, 0f, 0f)
            shader.render(box.mesh)

            box.mesh.worldMatrix?.setToTranslation(1.5f, 0f, 0f)
            shader.render(box.mesh)
        }
    }
}
