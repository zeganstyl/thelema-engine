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

package org.ksdfv.thelema.test.shaders.glsl

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.gltf.GLTFConf
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.test.GLTFModel
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.texture.GBuffer

/** @author zeganstyl */
class GBufferTest: Test {
    override val name: String
        get() = "G-Buffer"

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

        val model = GLTFModel(conf = GLTFConf(separateThread = false, setupGBufferShader = true))

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            gBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                model.update()
                model.render()
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
