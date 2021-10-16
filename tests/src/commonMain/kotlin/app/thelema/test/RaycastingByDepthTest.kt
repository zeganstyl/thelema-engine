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

package app.thelema.test

import app.thelema.app.APP
import app.thelema.data.DATA
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.gl.*
import app.thelema.img.DepthFrameBuffer
import app.thelema.img.render
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.input.BUTTON
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.shader.Shader
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.post.PostShader
import app.thelema.utils.Color
import app.thelema.utils.LOG

class RaycastingByDepthTest: Test {
    override fun testMain() {
        val depthBuffer = DepthFrameBuffer()

        val plane = PlaneMesh { setSize(10f) }
        val box = BoxMesh { setSize(2f) }
        val boxSmall = BoxMesh { setSize(0.2f) }
        boxSmall.mesh.worldMatrix = Mat4()

        val sceneShader = SimpleShader3D()

        val depthRenderShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform mat4 viewProj;
void main() {
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
void main() {}
"""
        )

        val depthPackingShader = PostShader(
            """
varying vec2 uv;

uniform sampler2D tex;

const vec4 bitEnc = vec4(1.,255.,65025.,16581375.);
const vec4 bitDec = 1./bitEnc;
vec4 EncodeFloatRGBA (float v) {
    vec4 enc = bitEnc * v;
    enc = fract(enc);
    enc -= enc.yzww * vec2(1./255., 0.).xxxy;
    return enc;
}

void main() {
    gl_FragColor = EncodeFloatRGBA(texture2D(tex, uv).x);
}
"""
        )

        val pixels = DATA.bytes(GL.mainFrameBufferWidth * GL.mainFrameBufferHeight * 4)

        val bitDec = Vec4(1f,1f / 255f,1f / 65025f,1f / 16581375f)
        val pixel = Vec4()
        val pos = Vec3()

        MOUSE.addListener(object : IMouseListener {
            override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
                if (MOUSE.isButtonPressed(BUTTON.RIGHT)) buttonUp(BUTTON.RIGHT, screenX, screenY, pointer)
            }

            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                if (button != BUTTON.RIGHT) return

                // unpack depth
                val pixelStart = ((APP.height - screenY) * GL.mainFrameBufferWidth + screenX) * 4
                pixel.x = (pixels[pixelStart].toInt() and 0xFF).toFloat() / 255f
                pixel.y = (pixels[pixelStart + 1].toInt() and 0xFF).toFloat() / 255f
                pixel.z = (pixels[pixelStart + 2].toInt() and 0xFF).toFloat() / 255f
                pixel.w = (pixels[pixelStart + 3].toInt() and 0xFF).toFloat() / 255f
                val depth = pixel.dot(bitDec)

                if (depth > 0f) {
                    // convert coordinates to NDC and project by inverse matrix
                    pos.x = 2f * screenX.toFloat() / APP.width.toFloat() - 1f
                    pos.y = 2f * (APP.height - screenY).toFloat() / APP.height.toFloat() - 1f
                    pos.z = 2f * depth - 1f
                    pos.prj(ActiveCamera.inverseViewProjectionMatrix)

                    // apply position
                    boxSmall.mesh.worldMatrix?.setToTranslation(pos)
                }
            }
        })

        LOG.info("Use right mouse button to drag box")

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            // disable alpha blending, because we will use alpha channel for depth packing
            GL.isBlendingEnabled = false

            // render depth
            depthBuffer.render {
                depthRenderShader.bind()
                depthRenderShader["viewProj"] = ActiveCamera.viewProjectionMatrix
                plane.render(depthRenderShader)
                box.render(depthRenderShader)
            }

            // render to main frame buffer and read pixels from it
            depthPackingShader.bind()
            depthBuffer.texture.bind(0)
            ScreenQuad.render(depthPackingShader)
            GL.glReadPixels(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
            GL.glClearColor(Color.BLACK)
            GL.glClear()

            // render scene
            plane.render(sceneShader)
            box.render(sceneShader)
            boxSmall.render(sceneShader)
        }
    }
}