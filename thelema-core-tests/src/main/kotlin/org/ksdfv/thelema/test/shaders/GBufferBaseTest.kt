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

package org.ksdfv.thelema.test.shaders

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.texture.GBuffer

/** @author zeganstyl */
class GBufferBaseTest: Test {
    override val name: String
        get() = "G-Buffer base"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer < 3) {
                APP.messageBox("Not supported", "Requires GLES 3.0 (WebGL 2.0)")
                return
            }
            if (GL.glesMajVer == 3) {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }

        val screenQuad = ScreenQuad.TextureRenderer(0f, 0f, 0.5f, 0.5f)

        val gBuffer = GBuffer()

        ActiveCamera.api = Camera(
            from = Vec3(0f, 3f, -3f),
            to = IVec3.Zero,
            near = 2f,
            far = 5f
        )

        @Language("GLSL")
        val shader = Shader(version = 330,
            vertCode = """
in vec3 aPosition;
in vec2 aUV;
in vec3 aNormal;

uniform mat4 world;
uniform mat4 viewProj;

out vec2 vUV;
out vec3 vNormal;
out vec3 vPosition;

void main() {
vUV = aUV;
vNormal = (world * vec4(aNormal, 1.0)).xyz; // our cube is not translating, only rotating, so we can multiply normals with world matrix
vPosition = (world * vec4(aPosition, 1.0)).xyz;
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

        val cube = BoxMeshBuilder().build()

        val cubeMatrix = Mat4()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix.rotate(0f, 1f, 0f, APP.deltaTime)

            gBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                shader.bind()
                shader["world"] = cubeMatrix
                shader["viewProj"] = ActiveCamera.viewProjectionMatrix

                cube.render(shader)
            }

            screenQuad.render(gBuffer.colorMap, clearMask = null) {
                screenQuad.setPosition(-0.5f, 0.5f)
            }

            screenQuad.render(gBuffer.normalMap, clearMask = null) {
                screenQuad.setPosition(0.5f, 0.5f)
            }

            screenQuad.render(gBuffer.positionMap, clearMask = null) {
                screenQuad.setPosition(-0.5f, -0.5f)
            }

            screenQuad.render(gBuffer.depthMap, clearMask = null) {
                screenQuad.setPosition(0.5f, -0.5f)
            }
        }
    }
}
