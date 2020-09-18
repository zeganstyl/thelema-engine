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

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE
import org.ksdfv.thelema.img.IFrameBuffer
import org.ksdfv.thelema.img.ITexture
import org.ksdfv.thelema.img.SimpleFrameBuffer
import org.ksdfv.thelema.math.IVec2
import org.ksdfv.thelema.math.Vec2
import org.ksdfv.thelema.mesh.IScreenQuad
import kotlin.math.pow

// https://catlikecoding.com/unity/tutorials/advanced-rendering/bloom/

/** @author zeganstyl */
class Bloom(
    width: Int,
    height: Int,
    iterations: Int = 6,
    iterationsStep: Int = 1,
    intensity: Float = 1f,
    pixelFormat: Int = GL_RGBA,
    internalFormat: Int = GL_RGBA,
    pixelChannelType: Int = GL_UNSIGNED_BYTE
) {
    private var downBuffers = Array(iterations) {
        val div = 2f.pow(it + 1).toInt() * iterationsStep
        SimpleFrameBuffer(
            width / div,
            height / div,
            pixelFormat,
            internalFormat,
            pixelChannelType,
            hasDepth = false
        )
    }

    private var upBuffers = Array(iterations) {
        val div = 2f.pow(it + 1).toInt() * iterationsStep
        SimpleFrameBuffer(
            width / div,
            height / div,
            pixelFormat,
            internalFormat,
            pixelChannelType,
            hasDepth = false
        )
    }

    private var texelSizes = Array<IVec2>(iterations) { Vec2(1f / downBuffers[it].width, 1f / downBuffers[it].height) }

    val blurDown = PostShader(blurDownCode)

    val blurUp = PostShader(blurUpCode)

    var intensity: Float = intensity
        set(value) {
            field = value
            blurUp.bind()
            blurUp["uIntensity"] = intensity
        }

    init {
        blurDown.bind()
        blurDown["uTexture"] = 0

        blurUp.bind()
        blurUp["uTexture"] = 0
        blurUp["uSourceTexture"] = 1
        blurUp["uIntensity"] = intensity
    }

    fun initBuffers(
        width: Int,
        height: Int,
        iterations: Int = 6,
        iterationsStep: Int = 1,
        pixelFormat: Int = GL_RGBA,
        internalFormat: Int = pixelFormat,
        pixelType: Int = GL_UNSIGNED_BYTE
    ) {
        val downBuffers = downBuffers
        for (i in downBuffers.indices) {
            downBuffers[i].destroy()
        }

        val upBuffers = upBuffers
        for (i in upBuffers.indices) {
            upBuffers[i].destroy()
        }

        this.downBuffers = Array(iterations) {
            val div = 2f.pow(it + 1).toInt() * iterationsStep
            SimpleFrameBuffer(
                width / div,
                height / div,
                pixelFormat,
                internalFormat,
                pixelType,
                hasDepth = false
            )
        }

        this.upBuffers = Array(iterations) {
            val div = 2f.pow(it + 1).toInt() * iterationsStep
            SimpleFrameBuffer(
                width / div,
                height / div,
                pixelFormat,
                internalFormat,
                pixelType,
                hasDepth = false
            )
        }

        texelSizes = Array(iterations) { Vec2(1f / downBuffers[it].width, 1f / downBuffers[it].height) }
    }

    fun render(screenQuad: IScreenQuad, sceneMap: ITexture, brightnessMap: ITexture, out: IFrameBuffer?) {
        val downBuffers = downBuffers
        val upBuffers = upBuffers
        val texelSizes = texelSizes

        // downscale
        blurDown.bind()
        blurDown["uDelta"] = 1f

        var prevMap = brightnessMap
        for (i in downBuffers.indices) {
            val buffer = downBuffers[i]
            screenQuad.render(blurDown, buffer) {
                blurDown["uTexelSize"] = texelSizes[i]
                prevMap.bind(0)
            }
            prevMap = buffer.getTexture(0)
        }

        // upscale
        blurUp.bind()
        blurUp["uDelta"] = 0.5f

        var i = downBuffers.size - 1
        while (i > 0) {
            screenQuad.render(blurUp, upBuffers[i-1], clearMask = null) {
                blurUp["uTexelSize"] = texelSizes[i-1]
                downBuffers[i].getTexture(0).bind(0)
                upBuffers[i].getTexture(0).bind(1)
            }

            i--
        }

        screenQuad.render(blurUp, out) {
            blurUp.set("uTexelSize", 1f / (out?.width ?: GL.mainFrameBufferWidth), 1f / (out?.height ?: GL.mainFrameBufferHeight))
            upBuffers[0].getTexture(0).bind(0)
            sceneMap.bind(1)
        }
    }

    fun destroy() {
        for (i in downBuffers.indices) {
            downBuffers[i].destroy()
        }
        downBuffers = Array(0) { SimpleFrameBuffer(0, 0) }
        texelSizes = Array(0) { Vec2() }

        blurDown.destroy()
        blurUp.destroy()
    }

    companion object {
        const val blurDownCode: String = """
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

        const val blurUpCode: String = """
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
    }
}
