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
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.mesh.IScreenQuad
import org.ksdfv.thelema.texture.IFrameBuffer
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
class MotionBlur(
    var sceneColorMap: ITexture? = null,
    var velocityMap: ITexture? = null,
    blurStrength: Float = 1f,
    numSamples: Int = 8,
    visualizeVelocity: Float = 0f
): PostShader(motionBlurCode(visualizeVelocity)) {
    var blurStrength = blurStrength
        set(value) {
            if (field != value) {
                field = value
                this["uBlurStrength"] = value
            }
        }

    var numSamples = numSamples
        set(value) {
            if (field != value) {
                field = value
                this["uNumSamples"] = value
            }
        }

    val sceneColorMapUnit = GL.grabTextureUnit()
    val velocityMapUnit = GL.grabTextureUnit()

    init {
        bind()
        this["uSceneColorMap"] = sceneColorMapUnit
        this["uVelocityMap"] = velocityMapUnit
        this["uBlurStrength"] = blurStrength
        this["uNumSamples"] = numSamples
    }

    fun render(screenQuad: IScreenQuad, out: IFrameBuffer?) {
        screenQuad.render(this, out) {
            sceneColorMap?.bind(sceneColorMapUnit)
            velocityMap?.bind(velocityMapUnit)
        }
    }

    companion object {
        private fun velocity(visualizeVelocity: Float): String {
            var str = ""
            if (visualizeVelocity > 0f) {
                str += "gl_FragColor.x += velocity.x * 20;\n"
                str += "gl_FragColor.y += velocity.y * 20;\n"
            }
            return str
        }

        @Language("GLSL")
        fun motionBlurCode(visualizeVelocity: Float = 0f): String {
            // http://john-chapman-graphics.blogspot.com/2013/01/what-is-motion-blur-motion-pictures-are.html
            return """
varying vec2 uv;
uniform sampler2D uSceneColorMap;
uniform sampler2D uVelocityMap;

uniform int uNumSamples;
const vec3 VZERO = vec3(0.0, 0.0, 0.0);

uniform float uBlurStrength;

void main() {
    vec3 vSum = VZERO;

    vec2 velocity = texture2D(uVelocityMap, uv).xy * 0.5;    
    //velocity *= uBlurStrength;
    if (velocity.x != 0 || velocity.y != 0) {
        float fNumSamples = float(uNumSamples) - 1.0;

        for (int k = 0; k < uNumSamples; ++k) {; 
            vec2 offset = velocity * (float(k) / fNumSamples - 0.5);        
            vSum += texture2D(uSceneColorMap, uv + offset);
        }

        vSum /= uNumSamples;

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
