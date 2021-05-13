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
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.gl.*
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class InstancingTest: Test {
    override val name: String
        get() = "Instancing"

    override fun testMain() {

        val simpleShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform mat4 viewProj;

void main() {
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
void main() {
    gl_FragColor = vec4(1.0);
}""")


        val instancedShader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec3 INSTANCE_POSITION;
uniform mat4 viewProj;

void main() {
    gl_Position = viewProj * vec4(POSITION + INSTANCE_POSITION, 1.0);
}""",
            fragCode = """
void main() {
    gl_FragColor = vec4(1.0, 0.5, 0.0, 1.0);
}""")

        val stepX = 5f
        val numX = 200
        val startX = -numX * stepX * 0.5f

        val stepZ = 5f
        val numZ = 200
        val startZ = -numZ * stepZ * 0.5f

        val cubesY = 1f

        val plane = PlaneMeshBuilder(width = 1000f, height = 1000f).apply {
            positionName = "POSITION"
        }.build()

        val cubes = BoxMeshBuilder().apply {
            positionName = "POSITION"
        }.build()

        cubes.instancesCountToRender = numX * numZ
        cubes.addVertexBuffer {
            addAttribute(3, "INSTANCE_POSITION")
            initVertexBuffer(numX * numZ) {
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

            loadBufferToGpu()
        }

        GL.isDepthTestEnabled = true

        ActiveCamera {
            near = 0.1f
            far = 1000f
        }

        val control = OrbitCameraControl(targetDistance = 100f, camera = ActiveCamera)
        control.listenToMouse()
        LOG.info(control.help)

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            simpleShader.bind()
            simpleShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            plane.render(simpleShader)

            instancedShader.bind()
            instancedShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            cubes.render(instancedShader)
        }
    }
}
