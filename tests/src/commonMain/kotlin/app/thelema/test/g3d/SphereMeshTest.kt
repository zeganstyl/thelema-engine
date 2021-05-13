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

package app.thelema.test.g3d

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.GL
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

/** @author zeganstyl */
class SphereMeshTest: Test {
    override val name: String
        get() = "Sphere mesh"

    override fun testMain() {
        val sphere = SphereMesh { setSize(2f) }

        val shader = SimpleShader3D {
            positionName = sphere.builder.positionName
            uvName = sphere.builder.uvName
            renderAttributeName = uvName
            worldMatrix = Mat4()
        }

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            updateCamera()
        }

        APP.onRender = {
            GL.glClear()

            shader.worldMatrix?.rotate(1f, 1f, 0f, APP.deltaTime)

            shader.render(sphere.mesh)
        }
    }
}
