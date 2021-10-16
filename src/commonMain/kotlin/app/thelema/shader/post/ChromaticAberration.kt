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

// https://github.com/nekocode/CameraFilter
/** @author zeganstyl */
class ChromaticAberration: PostShader(
    fragCode = """
uniform sampler2D uColorMap;
varying vec2 uv;

uniform float uStrength;

void main() {
    vec3 col;
    col.r = texture2D( uColorMap, vec2(uv.x+uStrength,uv.y) ).r;
    col.g = texture2D( uColorMap, uv ).g;
    col.b = texture2D( uColorMap, vec2(uv.x-uStrength,uv.y) ).b;

    col *= (1.0 - uStrength * 0.5);

    gl_FragColor = vec4(col,1.0);
}
"""
) {
    var strength: Float = 0.02f

    init {
        bind()
        set("uColorMap", 0)
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        set("uStrength", strength)
        ScreenQuad.render(this, out)
    }
}
