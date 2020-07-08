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
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.input.IKeyListener
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.mesh.build.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.post.MotionBlur
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.texture.FrameBuffer
import kotlin.math.abs

object MotionBlurBaseTest: Test("Motion Blur Base") {
    override fun testMain() {
        @Language("GLSL")
        val sceneColorShader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform vec3 pos;
uniform mat4 viewProj;

void main() {
gl_Position = viewProj * vec4(aPosition + pos, 1.0);
}""",
            fragCode = """
uniform vec3 color;

void main() {
gl_FragColor = vec4(color, 1.0);
}""")

        @Language("GLSL")
        val velocityShader = Shader(
            vertCode = """
attribute vec3 aPosition;
attribute vec3 aNormal;

uniform vec3 prevPos;
uniform vec3 pos;

uniform mat4 viewProj;
uniform mat4 prevViewProj;

varying vec4 clipPos;
varying vec4 prevClipPos;

void main() {
// get world space vertex positions
vec3 worldPos = aPosition + pos;
vec3 prevWorldPos = aPosition + prevPos;

// get clip space vertex positions
clipPos = viewProj * vec4(worldPos, 1.0);
prevClipPos = prevViewProj * vec4(prevWorldPos, 1.0);

// https://www.nvidia.com/docs/IO/8230/GDC2003_OpenGLShaderTricks.pdf
// apply stretching
if (dot(worldPos.xyz - prevWorldPos.xyz, aNormal) > 0.0) {
    gl_Position = clipPos;
} else {
    gl_Position = prevClipPos;
}
}""",
            fragCode = """
varying vec4 clipPos;
varying vec4 prevClipPos;

void main() {
// get NDC space vertex positions.
// it must be calculated in fragment shader for correct interpolation.
vec2 ndcPos = (clipPos / clipPos.w).xy;
vec2 prevNdcPos = (prevClipPos / prevClipPos.w).xy;

gl_FragColor = vec4(ndcPos - prevNdcPos, 0.0, 1.0);
}""")

        ActiveCamera.api = Camera(from = Vec3(5f, 5f, 0f), to = IVec3.Zero, near = 0.1f, far = 100f)

        val plane = PlaneMeshBuilder(width = 5f, height = 5f).build()

        val screenQuad = ScreenQuad.TextureRenderer()

        val sceneColorBuffer = FrameBuffer(APP.width, APP.height, GL_RGB)
        val velocityMapBuffer = FrameBuffer(APP.width, APP.height, GL_RG, GL_RG16F, GL_FLOAT)

        val motionBlur = MotionBlur(
            sceneColorBuffer.getTexture(0),
            velocityMapBuffer.getTexture(0),
            1f,
            16
        )

        val cube = BoxMeshBuilder().build()
        val cubePos = Vec3(0f, 1f, 0f)
        val cubePrevPos = Vec3(cubePos)

        GL.isDepthTestEnabled = true

        var moveDir = 1f

        var movingEnabled = true

        KB.addListener(object : IKeyListener {
            override fun keyDown(keycode: Int) {
                movingEnabled = !movingEnabled
            }
        })

        println("Press 'Space' to pause/resume")

        val cubeColor = Vec3(1f, 0.5f, 0f)

        ActiveCamera.updatePreviousTransform()

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            if (movingEnabled) {
                val delta = APP.deltaTime * 10f
                cubePos.z += delta * moveDir

                if (abs(cubePos.z) > 3f) {
                    moveDir *= -1f
                }

                velocityMapBuffer.render {
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                    // in this example previous view and current view matrices are equal, because camera is not moving
                    velocityShader.bind()
                    velocityShader["viewProj"] = ActiveCamera.viewProjectionMatrix
                    velocityShader["prevViewProj"] = ActiveCamera.previousViewProjectionMatrix

                    velocityShader["pos"] = cubePos
                    velocityShader["prevPos"] = cubePrevPos
                    cube.render(velocityShader)

                    velocityShader["pos"] = IVec3.Zero
                    velocityShader["prevPos"] = IVec3.Zero
                    plane.render(velocityShader)
                }
            }

            sceneColorBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                sceneColorShader.bind()
                sceneColorShader["viewProj"] = ActiveCamera.viewProjectionMatrix

                sceneColorShader["pos"] = cubePos
                sceneColorShader["color"] = cubeColor
                cube.render(sceneColorShader)

                sceneColorShader["pos"] = IVec3.Zero
                sceneColorShader["color"] = IVec3.One
                plane.render(sceneColorShader)
            }

            //screenQuad.render(velocityMapBuffer.getTexture(0))
            motionBlur.render(screenQuad, null)

            if (movingEnabled) {
                cubePrevPos.set(cubePos)
            }
        }
    }
}