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

package org.ksdfv.thelema.test.shader.post


import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.img.GBuffer
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.mesh.gen.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.post.SSAO
import org.ksdfv.thelema.test.Test

/** @author zeganstyl */
class SSAOBaseTest: Test {
    override val name: String
        get() = "SSAO Base"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer < 3) {
                APP.messageBox("Not supported", "Requires GLES 3.0 (WebGL 2.0)")
                return
            } else {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }


        val sceneShader = Shader(version = 330,
            vertCode = """
in vec3 aPosition;
in vec2 aUV;
in vec3 aNormal;

uniform mat4 world;
uniform mat4 view;
uniform mat4 viewProj;

out vec2 vUV;
out vec3 vNormal;
out vec3 vViewSpacePosition;

void main() {
vUV = aUV;
vNormal = (world * vec4(aNormal, 1.0)).xyz; // our cube is not translating, only rotating, so we can multiply normals with world matrix
vec4 worldPos = world * vec4(aPosition, 1.0);
vViewSpacePosition = (view * worldPos).xyz; // for ssao positions must in view space
gl_Position = viewProj * worldPos;
}""",
            fragCode = """
in vec2 vUV;
in vec3 vNormal;
in vec3 vViewSpacePosition;

layout (location = 0) out vec4 gColor;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec4 gPosition;

void main() {
gColor = vec4(vUV, 0.0, 1.0);
gNormal = vec4(vNormal, 1.0);
gPosition = vec4(vViewSpacePosition, 1.0);
}""")

        val screenQuad = ScreenQuad(0f, 0f, 1f, 1f)

        val gBuffer = GBuffer()

        val ssao = SSAO(gBuffer.colorMap, gBuffer.normalMap, gBuffer.positionMap)

        val camera = Camera(
            position = Vec3(0f, 3f, -3f),
            target = MATH.Zero3,
            near = 0.1f,
            far = 100f
        )

        val cube = BoxMeshBuilder().build()
        val plane = PlaneMeshBuilder(width = 5f, height = 5f).build()

        val cubeMatrix = Mat4()
        val planeMatrix = Mat4()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

        GL.render {
            GL.glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

            cubeMatrix.rotate(0f, 1f, 0f, APP.deltaTime)

            gBuffer.render {
                GL.glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

                sceneShader.bind()
                sceneShader["view"] = camera.viewMatrix
                sceneShader["viewProj"] = camera.viewProjectionMatrix

                sceneShader["world"] = cubeMatrix
                cube.render(sceneShader)

                sceneShader["world"] = planeMatrix
                plane.render(sceneShader)
            }

            ssao.render(screenQuad, null)
        }
    }
}
