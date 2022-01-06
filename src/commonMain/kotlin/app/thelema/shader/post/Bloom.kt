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

package app.thelema.shader.post

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.gl.*
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.img.SimpleFrameBuffer
import app.thelema.math.IVec2
import app.thelema.math.Vec2
import kotlin.math.pow

// https://catlikecoding.com/unity/tutorials/advanced-rendering/bloom/

/** @author zeganstyl */
class Bloom(
    width: Int,
    height: Int,
    iterations: Int = 5,
    stepOffset: Int = 2,
    intensity: Float = 1.25f,
    pixelFormat: Int = GL_RGBA,
    internalFormat: Int = GL_RGBA,
    pixelChannelType: Int = GL_UNSIGNED_BYTE
): IPostEffect, IEntityComponent {
    override val componentName: String
        get() = "Bloom"

    override var entityOrNull: IEntity? = null

    private var downBuffers = Array(iterations) {
        val div = 2f.pow(it + stepOffset).toInt()
        SimpleFrameBuffer(
            width / div,
            height / div,
            internalFormat,
            pixelFormat,
            pixelChannelType,
            hasDepth = false
        )
    }

    private var upBuffers = Array(iterations) {
        val div = 2f.pow(it + stepOffset).toInt()
        SimpleFrameBuffer(
            width / div,
            height / div,
            internalFormat,
            pixelFormat,
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

    var brightnessMap: ITexture? = null

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
        stepOffset: Int = 1,
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
            val div = 2f.pow(it + stepOffset).toInt()
            SimpleFrameBuffer(
                width / div,
                height / div,
                internalFormat,
                pixelFormat,
                pixelType,
                hasDepth = false
            )
        }

        this.upBuffers = Array(iterations) {
            val div = 2f.pow(it + stepOffset).toInt()
            SimpleFrameBuffer(
                width / div,
                height / div,
                internalFormat,
                pixelFormat,
                pixelType,
                hasDepth = false
            )
        }

        texelSizes = Array(iterations) { Vec2(1f / this.downBuffers[it].width, 1f / this.downBuffers[it].height) }
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        val downBuffers = downBuffers
        val upBuffers = upBuffers
        val texelSizes = texelSizes

        // downscale
        blurDown.bind()
        blurDown["uDelta"] = 1f

        var prevMap = brightnessMap ?: throw IllegalStateException("Bloom: Brightness map must be set")
        for (i in downBuffers.indices) {
            val buffer = downBuffers[i]
            blurDown["uTexelSize"] = texelSizes[i]
            prevMap.bind(0)
            ScreenQuad.render(blurDown, buffer)
            prevMap = buffer.getTexture(0)
        }

        // upscale
        blurUp.bind()
        blurUp["uDelta"] = 0.5f

        var i = downBuffers.size - 1
        while (i > 0) {
            blurUp["uTexelSize"] = texelSizes[i-1]
            downBuffers[i].getTexture(0).bind(0)
            upBuffers[i].getTexture(0).bind(1)
            ScreenQuad.render(blurUp, upBuffers[i-1], clearMask = null)
            i--
        }

        blurUp.set("uTexelSize", 1f / (out?.width ?: GL.mainFrameBufferWidth), 1f / (out?.height ?: GL.mainFrameBufferHeight))
        upBuffers[0].getTexture(0).bind(0)
        inputMap.bind(1)
        ScreenQuad.render(blurUp, out)
    }

    override fun destroy() {
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
