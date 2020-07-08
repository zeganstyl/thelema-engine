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
class ChromaticAberration {
    var strength: Float = 0.02f
    private var uStrength: Int = 0

    val shader = PostShader(chromaticAberrationCode)

    init {
        shader.bind()
        uStrength = shader["uStrength"]

        shader["uColorMap"] = 0
        shader["uStrength"] = strength
    }

    fun render(screenQuad: IScreenQuad, inputMap: ITexture, out: IFrameBuffer?) {
        screenQuad.render(shader, out) {
            inputMap.bind(0)
            shader[uStrength] = strength
        }
    }

    fun dispose() {
        shader.destroy()
    }
    
    companion object {
        // https://github.com/nekocode/CameraFilter
        @Language("GLSL")
        val chromaticAberrationCode: String = """
uniform sampler2D uColorMap;
varying vec2 uv;

uniform float uStrength;

void main() {
    vec2 uv = uv.xy;

    vec3 col;
    col.r = texture2D( uColorMap, vec2(uv.x+uStrength,uv.y) ).r;
    col.g = texture2D( uColorMap, uv ).g;
    col.b = texture2D( uColorMap, vec2(uv.x-uStrength,uv.y) ).b;

    col *= (1.0 - uStrength * 0.5);

    gl_FragColor = vec4(col,1.0);
}"""
    }
}
