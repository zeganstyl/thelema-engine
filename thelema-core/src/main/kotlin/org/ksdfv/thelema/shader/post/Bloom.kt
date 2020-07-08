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

package org.ksdfv.thelema.shader.post

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.IVec2
import org.ksdfv.thelema.math.Vec2
import org.ksdfv.thelema.mesh.IScreenQuad
import org.ksdfv.thelema.texture.FrameBuffer
import org.ksdfv.thelema.texture.IFrameBuffer
import org.ksdfv.thelema.texture.ITexture
import kotlin.math.pow

// https://catlikecoding.com/unity/tutorials/advanced-rendering/bloom/

/** @author zeganstyl */
class Bloom(
    width: Int,
    height: Int,
    iterations: Int = 6,
    internalFormat: Int = GL_RGB16F,
    pixelType: Int = GL_UNSIGNED_BYTE
) {
    private var buffers = Array(iterations) {
        val div = 2f.pow(it + 1).toInt()
        FrameBuffer(
            width / div,
            height / div,
            GL_RGB,
            internalFormat,
            pixelType,
            hasDepth = false
        )
    }

    private var texelSizes = Array<IVec2>(iterations) { Vec2(1f / buffers[it].width, 1f / buffers[it].height) }

    val blurDown = PostShader(blurDownCode)

    val blurUp = PostShader(blurUpCode)

    private var uTexelSizeDown = 0
    private var uTexelSizeUp = 0
    private var uDelta = 0
    private var uIntensity = 0

    var intensity: Float = 1f
        set(value) {
            field = value
            blurUp.bind()
            blurUp[uIntensity] = intensity
        }

    init {
        GL.call {
            blurDown.bind()
            blurDown["uTexture"] = 0
            uTexelSizeDown = blurDown["uTexelSize"]
            uDelta = blurDown["uDelta"]

            blurUp.bind()
            blurUp["uTexture"] = 0
            blurUp["uSourceTexture"] = 1
            uTexelSizeUp = blurUp["uTexelSize"]
            uIntensity = blurUp["uIntensity"]
            blurUp[uIntensity] = intensity
        }
    }

    fun initBuffers(
        width: Int,
        height: Int,
        iterations: Int = 6,
        internalFormat: Int = GL_RGB16F,
        pixelType: Int = GL_FLOAT
    ) {
        for (i in buffers.indices) {
            buffers[i].destroy(true)
        }

        buffers = Array(iterations) {
            val div = 2f.pow(it + 1).toInt()
            FrameBuffer(
                width / div,
                height / div,
                GL_RGB,
                internalFormat,
                pixelType,
                hasDepth = false
            )
        }
        texelSizes = Array(iterations) { Vec2(1f / buffers[it].width, 1f / buffers[it].height) }
    }

    fun render(screenQuad: IScreenQuad, sceneMap: ITexture, brightnessMap: ITexture, out: IFrameBuffer?) {
        // downscale
        blurDown.bind()
        blurDown[uDelta] = 1f

        var prevMap = brightnessMap
        for (i in buffers.indices) {
            val buffer = buffers[i].getTexture(0)
            screenQuad.render(blurDown, buffers[i]) {
                blurDown[uTexelSizeDown] = texelSizes[i]
                prevMap.bind(0)
            }
            prevMap = buffer
        }

        // upscale
        blurDown.bind()
        blurDown[uDelta] = 0.5f

        var i = buffers.size - 1
        while (i > 0) {
            val b = buffers[i]
            val nextBuffer = buffers[i-1]
            screenQuad.render(blurUp, nextBuffer, clearMask = null) {
                blurUp[uTexelSizeUp] = texelSizes[i-1]
                b.getTexture(0).bind(0)
                nextBuffer.getTexture(0).bind(1)
            }

            i--
        }

        screenQuad.render(blurUp, out) {
            blurUp.set(uTexelSizeUp, 1f / (out?.width ?: GL.mainFrameBufferWidth), 1f / (out?.height ?: GL.mainFrameBufferHeight))
            buffers[0].getTexture(0).bind(0)
            sceneMap.bind(1)
        }
    }

    fun destroy() {
        for (i in buffers.indices) {
            buffers[i].destroy(true)
        }
        buffers = Array(0) { FrameBuffer(0, 0) }
        texelSizes = Array(0) { Vec2() }

        blurDown.destroy()
        blurUp.destroy()
    }

    companion object {
        @Language("GLSL")
        val blurDownCode: String = """
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
            }
        """.trimIndent()

        @Language("GLSL")
        val blurUpCode: String = """
            varying vec2 uv;
    
            uniform sampler2D uSourceTexture;
            uniform sampler2D uTexture;
    
            uniform float delta;
            uniform vec2 uTexelSize;
            uniform float uIntensity;
    
            void main() {
                vec4 o = uTexelSize.xyxy * vec2(-0.5, 0.5).xxyy;
    
                vec4 s =
                texture2D(uTexture, uv + o.xy) +
                texture2D(uTexture, uv + o.zy) +
                texture2D(uTexture, uv + o.xw) +
                texture2D(uTexture, uv + o.zw);
    
                gl_FragColor = vec4(s.rgb * 0.25 + texture2D(uSourceTexture, uv).rgb, 1.0);
                gl_FragColor.rgb *= uIntensity;
            }
        """.trimIndent()
    }
}
