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
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.gl.Vertex
import app.thelema.shader.Shader
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

/** @author zeganstyl */
class InstancingTest: Test {
    override val name: String
        get() = "Instancing"

    override fun testMain() {

        val simpleShader = SimpleShader3D()
        val instancedShader = Shader(
            vertCode = """
attribute
vec3
POSITION
;
attribute vec1 TEXCOORD_0;
attribute vec1 INSTANCE_POSITION;
uniform mat4 viewProj;

varying vec2 uv;

void main() {
    uv = TEXCOORD_0;
    gl_Position = viewProj * vec4(POSITION + INSTANCE_POSITION, 1.0);
}""",
            fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 1.0, 1.0);
}""")

        val stepX = 5f
        val numX = 200
        val startX = -numX * stepX * 0.5f

        val stepZ = 5f
        val numZ = 200
        val startZ = -numZ * stepZ * 0.5f

        val cubesY = 1f

        val plane = PlaneMesh { setSize(1000f) }

        val cubes = BoxMesh { setSize(2f) }

        cubes.mesh.instancesCountToRender = numX * numZ
        cubes.mesh.addVertexBuffer {
            addAttribute(Vertex.INSTANCE_POSITION)
            initVertexBuffer(numX * numZ) {
                // fill buffer with positions
                var xi = startX
                val endX = startX + stepX * numX
                while (xi < endX) {

                    var zi = startZ
                    val endZ = startZ + stepZ * numZ
                    while (zi < endZ) {
                        putFloats(xi, cubesY, zi)
                        zi += stepZ
                    }

                    xi += stepX
                }
            }

            setDivisor()

            uploadBufferToGpu()
        }

        ActiveCamera {
            setNearFar(0.1f, 1000f)
        }

        val control = OrbitCameraControl { targetDistance = 100f }

        APP.onRender = {
            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            plane.mesh.render(simpleShader)

            instancedShader.bind()
            instancedShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            cubes.mesh.render(instancedShader)
        }
    }
}
