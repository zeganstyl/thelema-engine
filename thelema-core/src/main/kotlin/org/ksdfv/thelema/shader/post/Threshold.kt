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
import org.ksdfv.thelema.mesh.IScreenQuad
import org.ksdfv.thelema.texture.IFrameBuffer
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
class Threshold {
    var cutoff = 0.5f
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

    fun render(screenQuad: IScreenQuad, inputMap: ITexture, out: IFrameBuffer?) {
        screenQuad.render(shader, out) {
            inputMap.bind(0)
        }
    }

    fun dispose() {
        shader.destroy()
    }

    companion object {
        @Language("GLSL")
        const val thresholdCode = """
varying vec2 uv;
uniform sampler2D tex;

uniform float cutoff;

void main() {
    vec4 color = texture(tex, uv);

    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > cutoff) {
        gl_FragColor = vec4(color.rgb, 1.0);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}"""
    }
}