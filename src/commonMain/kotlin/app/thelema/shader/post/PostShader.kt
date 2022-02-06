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

import app.thelema.gl.ScreenQuad
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.shader.Shader

/** @author zeganstyl */
open class PostShader(
    fragCode: String,
    compile: Boolean = true,
    defaultPrecision: String = "mediump",
    uvName: String = "uv",
    attributeUVName: String = "UV",
    attributePositionName: String = "POSITION",
    version: Int = 330,
    flipY: Boolean = false
) :
    Shader(
        vertCode = """
in vec2 $attributePositionName;
in vec2 $attributeUVName;
out vec2 $uvName;

uniform bool flipY;

void main() {
    ${
        if (flipY) {
            "$uvName = vec2($attributeUVName.x, 1.0 - $attributeUVName.y);"
        } else {
            "$uvName = $attributeUVName;"
        }
    }
    gl_Position = vec4($attributePositionName, 0.0, 1.0);
}""",
        fragCode = fragCode,
        compile = compile,
        floatPrecision = defaultPrecision,
        version = version
    ), IPostEffect
{
    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        ScreenQuad.render(this, out)
    }
}
