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
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.ITexture
import app.thelema.img.SimpleFrameBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.math.Vec2
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.TextureRenderer
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.post.PostShader
import app.thelema.test.Test
import kotlin.math.pow

/** @author zeganstyl */
class BloomBaseTest: Test {
    override val name: String
        get() = "Bloom base"

    override fun testMain() {

        val cubeShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;
uniform mat4 viewProj;

void main() {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec3 vPosition;
                
void main() {
    gl_FragColor = vec4(vPosition, 1.0);
}"""
        )


        val blurDownShader = PostShader(
            fragCode = """
varying vec2 uv;

uniform sampler2D uTexture;

uniform vec2 uTexelSize;
uniform float uDelta;

void main() {
    vec4 o = uTexelSize.xyxy * vec2(-uDelta, uDelta).xxyy;

    vec4 s =
    texture2D(uTexture, uv + o.xy) +
    texture2D(uTexture, uv + o.zy) +
    texture2D(uTexture, uv + o.xw) +
    texture2D(uTexture, uv + o.zw);

    gl_FragColor = vec4(s.rgb * 0.25, 1.0);
}"""
        )

        blurDownShader.bind()
        blurDownShader["uTexture"] = 0


        val blurUpShader = PostShader(
            fragCode = """
varying vec2 uv;

uniform sampler2D uSourceTexture;
uniform sampler2D uTexture;

uniform float uDelta;
uniform vec2 uTexelSize;
uniform float uIntensity;

void main() {
    vec4 o = uTexelSize.xyxy * vec2(-uDelta, uDelta).xxyy;

    vec4 s =
    texture2D(uTexture, uv + o.xy) +
    texture2D(uTexture, uv + o.zy) +
    texture2D(uTexture, uv + o.xw) +
    texture2D(uTexture, uv + o.zw);

    gl_FragColor = vec4(s.rgb * 0.25 + texture2D(uSourceTexture, uv).rgb, 1.0);
    gl_FragColor.rgb *= uIntensity;
}"""
        )

        blurUpShader.bind()
        blurUpShader["uSourceTexture"] = 0
        blurUpShader["uTexture"] = 1
        blurUpShader["uIntensity"] = 1f

        val sceneColorBuffer = SimpleFrameBuffer()

        val iterations = 8

        val downScaleBuffers = Array(iterations) {
            val div = 2f.pow(it + 1).toInt()
            SimpleFrameBuffer(sceneColorBuffer.width / div, sceneColorBuffer.height / div)
        }

        val upScaleBuffers = Array(iterations) {
            val div = 2f.pow(it + 1).toInt()
            SimpleFrameBuffer(sceneColorBuffer.width / div, sceneColorBuffer.height / div)
        }

        val texelSizes = Array(iterations) { Vec2(1f / downScaleBuffers[it].width, 1f / downScaleBuffers[it].height) }

        val screenQuad = TextureRenderer()

        val box = BoxMeshBuilder().build()

        ActiveCamera {
            far = 1000f
        }

        val control = OrbitCameraControl(camera = ActiveCamera)
        control.listenToMouse()
        println(control.help)

        // maps to debug
        val debugMaps = ArrayList<ITexture>()
        debugMaps.addAll(downScaleBuffers)
        debugMaps.addAll(upScaleBuffers)
        var mapIndex = 0

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                if (button == MOUSE.LEFT) {
                    mapIndex = (mapIndex + 1) % debugMaps.size
                }
            }
        })

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            // draw scene to buffer
            sceneColorBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                cubeShader.bind()
                cubeShader["viewProj"] = ActiveCamera.viewProjectionMatrix
                box.render(cubeShader)
            }

            // downscale
            blurDownShader.bind()
            blurDownShader["uDelta"] = 1f

            var prevMap = sceneColorBuffer.getTexture(0) // brightness map
            for (i in downScaleBuffers.indices) {
                val buffer = downScaleBuffers[i]
                screenQuad.render(blurDownShader, buffer) {
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                    blurDownShader["uTexelSize"] = texelSizes[i]
                    prevMap.bind(0)
                }
                prevMap = buffer.getTexture(0)
            }

            // upscale
            blurUpShader.bind()
            blurUpShader["uDelta"] = 0.5f

            var i = iterations - 1
            while (i > 0) {
                screenQuad.render(blurUpShader, upScaleBuffers[i-1]) {
                    blurUpShader["uTexelSize"] = texelSizes[i-1]
                    downScaleBuffers[i].texture.bind(0)
                    upScaleBuffers[i].texture.bind(1)
                }

                i--
            }

            // bloom output
            screenQuad.render(blurUpShader, null) {
                blurUpShader.set("uTexelSize", 1f / APP.width, 1f / APP.height)
                upScaleBuffers[0].texture.bind(0)
                sceneColorBuffer.texture.bind(1)
            }

            // maps debug rendering
            //screenQuad.render(debugMaps[mapIndex])
        }
    }
}