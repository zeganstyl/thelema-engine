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

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.terrain.TerrainTileMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_LINES
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

class TileMeshBuilderTest: Test {
    override val name: String
        get() = "Tile mesh builder"

    override fun testMain() {
        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
varying vec2 uv;
uniform mat4 projViewModelTrans;

void main() {
    uv = POSITION.xz;
    gl_Position = projViewModelTrans * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(1.0);
}""")

        val mesh = TerrainTileMeshBuilder(4f, 10, 2).apply {
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        mesh.primitiveType = GL_LINES
        mesh.indices = mesh.indices!!.trianglesToWireframe()

        ActiveCamera {
            lookAt(Vec3(0f, 10f, 0.001f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        GL.isDepthTestEnabled = true
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            shader.bind()
            shader["projViewModelTrans"] = ActiveCamera.viewProjectionMatrix
            mesh.render(shader)
        }
    }
}