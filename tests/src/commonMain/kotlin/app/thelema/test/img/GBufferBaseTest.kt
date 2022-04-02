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

package app.thelema.test.img


import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.gl.GL
import app.thelema.gl.ScreenQuad
import app.thelema.img.GBuffer
import app.thelema.img.render
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class GBufferBaseTest: Test {
    override val name: String
        get() = "G-Buffer base"

    override fun testMain() {
        val gBuffer = GBuffer()

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            near = 2f
            far = 5f
            updateCamera()
        }

        val shader = Shader(version = 330,
            vertCode = """
in vec3 POSITION;
in vec2 TEXCOORD_0;
in vec3 NORMAL;

uniform mat4 world;
uniform mat4 viewProj;

out vec2 vUV;
out vec3 vNormal;
out vec3 vPosition;

void main() {
    vUV = TEXCOORD_0;
    vNormal = (world * vec4(NORMAL, 1.0)).xyz;
    vPosition = (world * vec4(POSITION, 1.0)).xyz;
    gl_Position = viewProj * vec4(vPosition, 1.0);
}""",
            fragCode = """
in vec2 vUV;
in vec3 vNormal;
in vec3 vPosition;

layout (location = 0) out vec4 gColor;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec4 gPosition;

void main() {
    gColor = vec4(vUV, 0.0, 1.0);
    gNormal = vec4(vNormal, 1.0);
    gPosition = vec4(vPosition, 1.0);
}""")

        val cube = BoxMesh { setSize(2f) }

        val cubeMatrix = Mat4()

        GL.render {
            cubeMatrix.rotate(0f, 1f, 0f, APP.deltaTime)

            gBuffer.render {
                GL.glClear()

                shader.bind()
                shader["world"] = cubeMatrix
                shader["viewProj"] = ActiveCamera.viewProjectionMatrix

                cube.render(shader)
            }

            ScreenQuad.render(4, flipY = false, maxCellsInRow = 2) {
                when (it) {
                    0 -> gBuffer.colorMap
                    1 -> gBuffer.normalMap
                    2 -> gBuffer.positionMap
                    3 -> gBuffer.depthMap
                    else -> gBuffer.getTexture(it)
                }
            }
        }
    }
}
