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

import app.thelema.gl.IVertexLayout
import app.thelema.gl.ScreenQuad
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.shader.Shader

/** @author zeganstyl */
open class PostShader(
    fragCode: String,
    defaultPrecision: String = "mediump",
    uvName: String = "uv",
    version: Int = 330,
    flipY: Boolean = false
) :
    Shader(
        vertCode = """
in vec2 POSITION;
in vec2 UV;
out vec2 $uvName;

uniform bool flipY;

void main() {
    ${
        if (flipY) {
            "$uvName = vec2(UV.x, 1.0 - UV.y);"
        } else {
            "$uvName = UV;"
        }
    }
    gl_Position = vec4(POSITION, 0.0, 1.0);
}""",
        fragCode = fragCode,
        floatPrecision = defaultPrecision,
        version = version
    ), IPostEffect
{
    override var vertexLayout: IVertexLayout = ScreenQuad.Layout

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        ScreenQuad.render(this, out)
    }
}
