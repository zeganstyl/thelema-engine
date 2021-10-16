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
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.input.IMouseListener
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.img.render
import app.thelema.input.MOUSE
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
            enablePreviousMatrix()
            lookAt(Vec3(5f, 5f, 0f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
            updatePreviousTransform()
        }

        val plane = PlaneMesh { setSize(5f) }

        val sceneColorBuffer = SimpleFrameBuffer(width = APP.width, height = APP.height, pixelFormat = GL_RGBA)
        val velocityMapBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            internalFormat = GL_RG16F,
            pixelFormat = GL_RG,
            pixelChannelType = GL_FLOAT
        )

        val motionBlur = MotionBlur(1f, 16)
        motionBlur.velocityMap = velocityMapBuffer.texture

        val cube = BoxMesh { setSize(1f) }
        val cubePos = Vec3(0f, 1f, 0f)
        val cubePrevPos = Vec3(cubePos)
        var moveDir = 1f

        var movingEnabled = true

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                movingEnabled = !movingEnabled
            }
        })

        LOG.info("Click on screen to pause/resume")

        val cubeColor = Vec3(1f, 0.5f, 0f)

        GL.glClearColor(0f, 0f, 0f, 1f)

        APP.onRender = {
            if (movingEnabled) {
                val delta = APP.deltaTime * 10f
                cubePos.z += delta * moveDir

                if (abs(cubePos.z) > 3f) {
                    moveDir *= -1f
                }

                velocityMapBuffer.render {
                    // in this example previous view and current view matrices are equal, because camera is not moving
                    velocityShader.bind()
                    velocityShader["viewProj"] = ActiveCamera.viewProjectionMatrix
                    velocityShader["prevViewProj"] = ActiveCamera.previousViewMatrix ?: ActiveCamera.viewProjectionMatrix

                    velocityShader["pos"] = cubePos
                    velocityShader["prevPos"] = cubePrevPos
                    cube.render(velocityShader)

                    velocityShader["pos"] = MATH.Zero3
                    velocityShader["prevPos"] = MATH.Zero3
                    plane.render(velocityShader)
                }
            }

            sceneColorBuffer.render {
                sceneColorShader.bind()
                sceneColorShader["viewProj"] = ActiveCamera.viewProjectionMatrix

                sceneColorShader["pos"] = cubePos
                sceneColorShader["color"] = cubeColor
                cube.render(sceneColorShader)

                sceneColorShader["pos"] = MATH.Zero3
                sceneColorShader["color"] = MATH.One3
                plane.render(sceneColorShader)
            }

            motionBlur.render(sceneColorBuffer.texture, null)

            if (movingEnabled) {
                cubePrevPos.set(cubePos)
            }
        }
    }
}