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

package app.thelema.test.shader.post


import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.GBuffer
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.gl.ScreenQuad
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.post.SSAO
import app.thelema.test.Test

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
in vec3 POSITION;
in vec2 UV;
in vec3 NORMAL;

uniform mat4 world;
uniform mat4 view;
uniform mat4 viewProj;

out vec2 vUV;
out vec3 vNormal;
out vec3 vViewSpacePosition;

void main() {
vUV = UV;
vNormal = (world * vec4(NORMAL, 1.0)).xyz; // our cube is not translating, only rotating, so we can multiply normals with world matrix
vec4 worldPos = world * vec4(POSITION, 1.0);
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

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

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
                sceneShader["view"] = ActiveCamera.viewMatrix
                sceneShader["viewProj"] = ActiveCamera.viewProjectionMatrix

                sceneShader["world"] = cubeMatrix
                cube.render(sceneShader)

                sceneShader["world"] = planeMatrix
                plane.render(sceneShader)
            }

            ssao.render(screenQuad, null)
        }
    }
}
