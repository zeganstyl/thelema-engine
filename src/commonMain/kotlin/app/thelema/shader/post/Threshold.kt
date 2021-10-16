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
class Threshold(cutoff: Float = 1f) {
    var cutoff = cutoff
        set(value) {
            field = value
            shader.bind()
            shader["cutoff"] = value
        }

    val shader = PostShader(thresholdCode)

    init {
        shader.bind()
        shader["cutoff"] = cutoff
        shader["tex"] = 0
    }

    fun render(inputMap: ITexture, out: IFrameBuffer?) {
        shader.bind()
        inputMap.bind(0)
        ScreenQuad.render(shader, out)
    }

    fun dispose() {
        shader.destroy()
    }

    companion object {
        const val thresholdCode = """
varying vec2 uv;
uniform sampler2D tex;

uniform float cutoff;

void main() {
    vec4 color = texture2D(tex, uv);

    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > cutoff) {
        gl_FragColor = vec4(color.rgb, 1.0);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}"""
    }
}