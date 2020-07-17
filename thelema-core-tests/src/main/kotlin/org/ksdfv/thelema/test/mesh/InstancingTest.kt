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

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_FLOAT
import org.ksdfv.thelema.mesh.InstanceBufferObject
import org.ksdfv.thelema.mesh.VertexInput
import org.ksdfv.thelema.mesh.VertexInputs
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.mesh.build.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

class InstancingTest: Test("Instancing") {
    override fun testMain() {
        @Language("GLSL")
        val simpleShader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform mat4 viewProj;

void main() {
    gl_Position = viewProj * vec4(aPosition, 1.0);
}""",
            fragCode = """
void main() {
    gl_FragColor = vec4(1.0);
}""")

        @Language("GLSL")
        val instancedShader = Shader(
            vertCode = """
attribute vec3 aPosition;
attribute vec3 aInstancePosition;
uniform mat4 viewProj;

void main() {
    gl_Position = viewProj * vec4(aPosition + aInstancePosition, 1.0);
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

        val plane = PlaneMeshBuilder(width = 1000f, height = 1000f).build()

        val cubes = BoxMeshBuilder().build()

        cubes.instances = InstanceBufferObject(
            numVertices = numX * numZ,
            instancesToRender = numX * numZ,
            vertexInputs = VertexInputs(
                VertexInput(3, "aInstancePosition", GL_FLOAT, true)
            )
        ).apply {
            floatBuffer.apply {
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

        ActiveCamera.api = Camera(near = 0.1f, far = 1000f)

        val control = OrbitCameraControl(targetDistance = 100f)
        control.listenToMouse()
        LOG.info(control.help)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.update()

            simpleShader.bind()
            simpleShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            plane.render(simpleShader)

            instancedShader.bind()
            instancedShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            cubes.render(instancedShader)
        }
    }
}
