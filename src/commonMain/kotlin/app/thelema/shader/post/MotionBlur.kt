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

import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.gl.ScreenQuad

/** @author zeganstyl */
class MotionBlur(
    blurStrength: Float = 1f,
    numSamples: Int = 8,
    visualizeVelocity: Float = 0f
): PostShader(motionBlurCode(numSamples, visualizeVelocity)) {
    override val componentName: String
        get() = "MotionBlur"

    var velocityMap: ITexture? = null

    var blurStrength = blurStrength
        set(value) {
            if (field != value) {
                field = value
                this["uBlurStrength"] = value
            }
        }

    init {
        bind()
        this["uSceneColorMap"] = 0
        this["uVelocityMap"] = 1
        this["uBlurStrength"] = blurStrength
        this["uNumSamples"] = numSamples
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        velocityMap?.bind(1)
        ScreenQuad.render(this, out)
    }

    companion object {
        private fun velocity(visualizeVelocity: Float): String {
            var str = ""
            if (visualizeVelocity > 0f) {
                str += "gl_FragColor.x += velocity.x * $visualizeVelocity;\n"
                str += "gl_FragColor.y += velocity.y * $visualizeVelocity;\n"
            }
            return str
        }

        fun motionBlurCode(numSamples: Int, visualizeVelocity: Float = 0f): String {
            // http://john-chapman-graphics.blogspot.com/2013/01/what-is-motion-blur-motion-pictures-are.html
            return """
varying vec2 uv;
uniform sampler2D uSceneColorMap;
uniform sampler2D uVelocityMap;

const vec3 VZERO = vec3(0.0, 0.0, 0.0);

uniform float uBlurStrength;

void main() {
    vec3 vSum = VZERO;

    vec2 velocity = texture2D(uVelocityMap, uv).xy * 0.5;    
    velocity *= uBlurStrength;
    if (velocity.x != 0.0 || velocity.y != 0.0) {
        float fNumSamples = $numSamples.0 - 1.0;

        for (int k = 0; k < $numSamples; ++k) {; 
            vec2 offset = velocity * (float(k) / fNumSamples - 0.5);        
            vSum += texture2D(uSceneColorMap, uv + offset).xyz;
        }

        vSum /= $numSamples.0;

        gl_FragColor = vec4(vSum, 1.0);
    } else {
        gl_FragColor = texture2D(uSceneColorMap, uv);
    }

${(velocity(visualizeVelocity))}
}
"""
        }
    }
}
