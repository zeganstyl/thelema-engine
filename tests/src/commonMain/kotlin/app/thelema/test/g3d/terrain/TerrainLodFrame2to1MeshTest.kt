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

package app.thelema.test.g3d.terrain

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.terrain.TerrainLodFrame2to1Mesh
import app.thelema.gl.GL
import app.thelema.gl.GL_BACK
import app.thelema.gl.GL_FRONT
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import app.thelema.utils.Color

class TerrainLodFrame2to1MeshTest: Test {
    override fun testMain() {
        val shader = SimpleShader3D {
            setupOnlyColor(Color.WHITE)
        }

        val frame = TerrainLodFrame2to1Mesh {
            setSize(5f)
            setDivisions(10)
            bottom = false
            left = false
        }

        //frame.mesh.indices = frame.mesh.indices!!.trianglesToWireframe()

        ActiveCamera {
            lookAt(Vec3(0f, 10f, 0.001f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        APP.onRender = {
            GL.isCullFaceEnabled = true
            GL.cullFaceMode = 1029
            frame.render(shader)
        }
    }
}
