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

package org.ksdfv.thelema.shader

import org.ksdfv.thelema.IFrameBuffer
import org.ksdfv.thelema.mesh.IScreenQuad
import org.ksdfv.thelema.texture.ITexture
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
class Vignette {
    val shader = Shader(vignetteCode)

    private var uRadius = 0
    private var uSoftness = 0

    var radius: Float = 0.75f
    var softness: Float = 0.45f

    init {
        shader.bind()
        uRadius = shader["uRadius"]
        uSoftness = shader["uSoftness"]

        shader["uTexture"] = 0
    }

    fun render(screenQuad: IScreenQuad, inputMap: ITexture, out: IFrameBuffer?) {
        screenQuad.render(shader, out) {
            inputMap.bind(0)
            shader[uRadius] = radius
            shader[uSoftness] = softness
        }
    }

    fun dispose() {
        shader.destroy()
    }

    companion object {
        // https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson3
        @Language("GLSL")
        val vignetteCode: String = """
        uniform sampler2D uTexture;

        varying vec2 v_texCoords;

        //RADIUS of our vignette, where 0.5 results in a circle fitting the screen
        uniform float uRadius = 0.75;

        //softness of our vignette, between 0.0 and 1.0
        uniform float uSoftness = 0.45;

        void main() {
            //sample our texture
            vec4 texColor = texture2D(uTexture, v_texCoords);

            //determine center
            vec2 position = v_texCoords - vec2(0.5);

            //OPTIONAL: correct for aspect ratio
            //position.x *= resolution.x / resolution.y;

            //determine the vector length from center
            float len = length(position);

            //our vignette effect, using smoothstep
            float vignette = smoothstep(uRadius, uRadius-uSoftness, len);

            //apply our vignette with 50% opacity
            texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, 0.5);

            gl_FragColor = texColor;
        }
    """.trimIndent()
    }
}