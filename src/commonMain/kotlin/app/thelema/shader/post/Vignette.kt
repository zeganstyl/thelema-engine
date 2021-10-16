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
class Vignette: PostShader(vignetteCode) {
    var radius: Float = 0.75f
    var softness: Float = 0.45f

    init {
        bind()

        this["uTexture"] = 0
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        this["uRadius"] = radius
        this["uSoftness"] = softness
        ScreenQuad.render(this, out)
    }

    companion object {
        // https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson3
        val vignetteCode: String = """
        uniform sampler2D uTexture;

        varying vec2 uv;

        //RADIUS of our vignette, where 0.5 results in a circle fitting the screen
        uniform float uRadius;

        //softness of our vignette, between 0.0 and 1.0
        uniform float uSoftness;

        void main() {
            //sample our texture
            vec4 texColor = texture2D(uTexture, uv);

            //determine center
            vec2 position = uv - vec2(0.5);

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