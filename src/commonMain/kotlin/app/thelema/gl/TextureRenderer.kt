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

package app.thelema.gl

import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.shader.Shader

/** Shader renders whole texture as is. May be used for debugging */
class TextureRenderer(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 1f,
    height: Float = 1f,
    positionName: String = "POSITION",
    uvName: String = "UV",
    flipY: Boolean = false,
    uvScale: Float = 1f
) {
    val mesh: IMesh by lazy {
        Mesh {
            primitiveType = GL_TRIANGLE_FAN

            addVertexBuffer {
                addAttribute(Vertex.POSITION)
                addAttribute(Vertex.TEXCOORD_0)
                initVertexBuffer(4) {
                    putFloats(x - width, y - height,  0f, 0f)
                    putFloats(x + width, y - height,  1f, 0f)
                    putFloats(x + width, y + height,  1f, 1f)
                    putFloats(x - width, y + height,  0f, 1f)
                }
            }
        }
    }

    val shader = Shader(
        vertCode = """
attribute vec2 $positionName;
attribute vec2 $uvName;

uniform vec2 translation;
uniform vec2 scale;
varying vec2 uv;

void main() {
    uv = $uvName;
    gl_Position = vec4(translation.x + $positionName.x * scale.x, translation.y + $positionName.y * scale.y, 0.0, 1.0);
}""",
        fragCode = """            
varying vec2 uv;
uniform sampler2D tex;

void main() {
    vec2 uv2 = uv;
    ${if (flipY) "uv2.y = 1.0 - uv2.y;" else ""}
    gl_FragColor = texture2D(tex, uv2${if (uvScale != 1f) " * $uvScale" else ""});
}"""
    )
    init {
        shader.bind()
        setPosition(0f, 0f)
        setScale(1f, 1f)

        shader["tex"] = 0
    }

    fun setPosition(x: Float, y: Float) {
        shader.bind()
        shader.set("translation", x, y)
    }

    fun setScale(x: Float, y: Float) {
        shader.bind()
        shader.set("scale", x, y)
    }

    inline fun render(
        texture: ITexture,
        out: IFrameBuffer? = null,
        clearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT,
        set: TextureRenderer.() -> Unit = {}
    ) {
        shader.bind()
        texture.bind(0)
        set()
        ScreenQuad.render(shader, out, clearMask)
    }
}