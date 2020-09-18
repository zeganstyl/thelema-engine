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

package org.ksdfv.thelema.test.mesh


import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_FLOAT
import org.ksdfv.thelema.mesh.VertexBufferObject
import org.ksdfv.thelema.mesh.VertexInput
import org.ksdfv.thelema.mesh.VertexInputs
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.mesh.gen.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

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

        cubes.instances = VertexBufferObject(
            numVertices = numX * numZ,
            instancesToRender = numX * numZ,
            vertexInputs = VertexInputs(
                VertexInput(3, "INSTANCE_POSITION", GL_FLOAT, true)
            )
        ).apply {
            bytes.floatView().apply {
                position = 0

                var xi = startX
                val endX = startX + stepX * numX
                while (xi < endX) {

                    var zi = startZ
                    val endZ = startZ + stepZ * numZ
                    while (zi < endZ) {
                        put(xi, cubesY, zi)
                        zi += stepZ
                    }

                    xi += stepX
                }
            }

            loadBufferToGpu()
        }

        GL.isDepthTestEnabled = true

        val camera = Camera(near = 0.1f, far = 1000f)

        val control = OrbitCameraControl(targetDistance = 100f, camera = camera)
        control.listenToMouse()
        LOG.info(control.help)

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            camera.update()

            simpleShader.bind()
            simpleShader["viewProj"] = camera.viewProjectionMatrix
            plane.render(simpleShader)

            instancedShader.bind()
            instancedShader["viewProj"] = camera.viewProjectionMatrix
            cubes.render(instancedShader)
        }
    }
}
