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
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.gl.TextureRenderer
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.post.MotionBlur
import app.thelema.test.Test
import app.thelema.utils.LOG
import kotlin.math.abs

/** @author zeganstyl */
class MotionBlurBaseTest: Test {
    override val name: String
        get() = "Motion Blur Base"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer == 3) {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }


        val sceneColorShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform vec3 pos;
uniform mat4 viewProj;

void main() {
gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            fragCode = """
uniform vec3 color;

void main() {
gl_FragColor = vec4(color, 1.0);
}""")


        val velocityShader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec3 NORMAL;

uniform vec3 prevPos;
uniform vec3 pos;

uniform mat4 viewProj;
uniform mat4 prevViewProj;

varying vec4 clipPos;
varying vec4 prevClipPos;

void main() {
// get world space vertex positions
vec3 worldPos = POSITION + pos;
vec3 prevWorldPos = POSITION + prevPos;

// get clip space vertex positions
clipPos = viewProj * vec4(worldPos, 1.0);
prevClipPos = prevViewProj * vec4(prevWorldPos, 1.0);

// https://www.nvidia.com/docs/IO/8230/GDC2003_OpenGLShaderTricks.pdf
// apply stretching
if (dot(worldPos.xyz - prevWorldPos.xyz, NORMAL) > 0.0) {
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

        ActiveCamera {
            lookAt(Vec3(5f, 5f, 0f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val plane = PlaneMeshBuilder(width = 5f, height = 5f).build()

        val screenQuad = TextureRenderer()

        val sceneColorBuffer = SimpleFrameBuffer(width = APP.width, height = APP.height, pixelFormat = GL_RGBA)
        val velocityMapBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            pixelFormat = GL_RG,
            internalFormat = GL_RG16F,
            type = GL_FLOAT
        )

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

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                movingEnabled = !movingEnabled
            }
        })

        LOG.info("Click on screen to pause/resume")

        val cubeColor = Vec3(1f, 0.5f, 0f)

        ActiveCamera.updatePreviousTransform()

        GL.glClearColor(0f, 0f, 0f, 1f)

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

                    velocityShader["pos"] = MATH.Zero3
                    velocityShader["prevPos"] = MATH.Zero3
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

                sceneColorShader["pos"] = MATH.Zero3
                sceneColorShader["color"] = MATH.One3
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