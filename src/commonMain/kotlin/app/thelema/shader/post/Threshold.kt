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

import app.thelema.math.IVec3
import app.thelema.math.Vec3

/** @author zeganstyl */
class Threshold(cutoff: Float = 1f): PostShader(thresholdCode) {
    override val componentName: String
        get() = "Threshold"

    var cutoff = cutoff
        set(value) {
            field = value
            bind()
            this["cutoff"] = value
        }

    var thresholdColor: IVec3 = Vec3(1f)
        set(value) {
            field.set(value)
            bind()
            this["thresholdColor"] = value
        }

    init {
        bind()
        this["cutoff"] = cutoff
        this["thresholdColor"] = thresholdColor
        this["tex"] = 0
    }

    companion object {
        const val thresholdCode = """
varying vec2 uv;
uniform sampler2D tex;

uniform float cutoff;
uniform vec3 thresholdColor;

void main() {
    vec4 color = texture2D(tex, uv);

    //float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    float brightness = dot(color.rgb, thresholdColor);
    if (brightness > cutoff) {
        gl_FragColor = vec4(color.rgb, 1.0);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}"""
    }
}