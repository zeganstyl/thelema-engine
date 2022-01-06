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

import app.thelema.gl.GL
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.gl.ScreenQuad

/** @author zeganstyl */
class FXAA: PostShader(fxaaCode) {
    override val componentName: String
        get() = "FXAA"

    var lumaThreshold = 0.7f
        set(value) {
            field = value
            updateUniforms()
        }

    var spanMax = 8f
        set(value) {
            field = value
            updateUniforms()
        }

    var reduceMin = 8f
        set(value) {
            field = value
            updateUniforms()
        }

    var reduceMul = 2f
        set(value) {
            field = value
            updateUniforms()
        }

    private var oldWidth: Int = 0
    private var oldHeight: Int = 0

    init {
        bind()
        this["uColorMap"] = 0
        updateUniforms()
    }

    private fun updateUniforms() {
        bind()
        this["u_lumaThreshold"] = lumaThreshold
        this["u_maxSpan"] = spanMax
        this["u_mulReduce"] = 1f / reduceMul
        this["u_minReduce"] = 1f / reduceMin
    }

    private fun updateWidthHeight(width: Int, height: Int) {
        if (oldWidth != width || oldHeight != height) {
            oldWidth = width
            oldHeight = height
            set("u_viewportInverse", 1f / width, 1f / height)
        }
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        if (out != null) {
            updateWidthHeight(out.width, out.height)
        } else{
            updateWidthHeight(GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)
        }
        super.render(inputMap, out)
    }

    companion object {
        // https://github.com/McNopper/OpenGL/blob/master/Example42/shader/fxaa.frag.glsl
        val fxaaCode: String = """
varying vec2 uv;

uniform sampler2D uColorMap;

// The inverse of the viewport dimensions along X and Y
uniform vec2 u_viewportInverse;

uniform float u_lumaThreshold;
uniform float u_maxSpan;
uniform float u_mulReduce;
uniform float u_minReduce;

vec4 fxaa(sampler2D tex, vec2 coords, vec2 inverseVP) {
    vec3 rgbM = texture2D(tex, coords).rgb;

    // Sampling neighbour texels. Offsets are adapted to OpenGL texture coordinates.
    vec3 rgbNW = texture2D(tex, coords + vec2(-1.0, 1.0) * inverseVP).rgb;
    vec3 rgbNE = texture2D(tex, coords + vec2(1.0, 1.0) * inverseVP).rgb;
    vec3 rgbSW = texture2D(tex, coords + vec2(-1.0, -1.0) * inverseVP).rgb;
    vec3 rgbSE = texture2D(tex, coords + vec2(1.0, -1.0) * inverseVP).rgb;

    // see http://en.wikipedia.org/wiki/Grayscale
    const vec3 toLuma = vec3(0.299, 0.587, 0.114);

    // Convert from RGB to luma.
    float lumaNW = dot(rgbNW, toLuma);
    float lumaNE = dot(rgbNE, toLuma);
    float lumaSW = dot(rgbSW, toLuma);
    float lumaSE = dot(rgbSE, toLuma);
    float lumaM = dot(rgbM, toLuma);

    // Gather minimum and maximum luma.
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    // If contrast is lower than a maximum threshold ...
    if (lumaMax - lumaMin <= lumaMax * u_lumaThreshold) {
        return vec4(rgbM, 1.0);
    }

    // Sampling is done along the gradient.
    vec2 samplingDirection;
    samplingDirection.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    samplingDirection.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

    // Sampling step distance depends on the luma: The brighter the sampled texels, the smaller the final sampling step direction.
    // This results, that brighter areas are less blurred/more sharper than dark areas.
    float samplingDirectionReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * 0.25 * u_mulReduce, u_minReduce);

    // Factor for norming the sampling direction plus adding the brightness influence.
    float minSamplingDirectionFactor = 1.0 / (min(abs(samplingDirection.x), abs(samplingDirection.y)) + samplingDirectionReduce);

    // Calculate final sampling direction vector by reducing, clamping to a range and finally adapting to the texture size.
    samplingDirection = clamp(samplingDirection * minSamplingDirectionFactor, vec2(-u_maxSpan), vec2(u_maxSpan)) * inverseVP;

    // Inner samples on the tab.
    vec3 rgbSampleNeg = texture2D(tex, coords + samplingDirection * (1.0/3.0 - 0.5)).rgb;
    vec3 rgbSamplePos = texture2D(tex, coords + samplingDirection * (2.0/3.0 - 0.5)).rgb;

    vec3 rgbTwoTab = (rgbSamplePos + rgbSampleNeg) * 0.5;

    // Outer samples on the tab.
    vec3 rgbSampleNegOuter = texture2D(tex, coords + samplingDirection * (0.0/3.0 - 0.5)).rgb;
    vec3 rgbSamplePosOuter = texture2D(tex, coords + samplingDirection * (3.0/3.0 - 0.5)).rgb;

    vec3 rgbFourTab = (rgbSamplePosOuter + rgbSampleNegOuter) * 0.25 + rgbTwoTab * 0.5;

    // Calculate luma for checking against the minimum and maximum value.
    float lumaFourTab = dot(rgbFourTab, toLuma);

    // Show edges for debug purposes.
//    rgbTwoTab.r = 1.0;
//    rgbFourTab.r = 1.0;

    // Are outer samples of the tab beyond the edge ...
    if (lumaFourTab < lumaMin || lumaFourTab > lumaMax) {
        // ... yes, so use only two samples.
        return vec4(rgbTwoTab, 1.0);
    } else {
        // ... no, so use four samples.
        return vec4(rgbFourTab, 1.0);
    }
}

void main() {
    gl_FragColor = fxaa(uColorMap, uv, u_viewportInverse);
}
"""
    }
}