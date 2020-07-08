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

package org.ksdfv.thelema.mesh

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.texture.IFrameBuffer
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
open class ScreenQuad(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 1f,
    height: Float = 1f,
    aPositionName: String = "aPosition",
    aUVName: String = "aUV"
): IScreenQuad {
    override val mesh: IMesh = IMesh.Build().apply {
        primitiveType = GL_TRIANGLE_FAN

        val vertexInputs = VertexInputs(
            VertexInput(2, aPositionName, GL_FLOAT, true),
            VertexInput(2, aUVName, GL_FLOAT, true)
        )

        vertices = IVertexBuffer.Build(
            DATA.bytes(16 * 4).apply {
                floatView().apply {
                    // x, y, z,   u, v
                    put(x + -width, y + -height,  0f, 0f)
                    put(x + width, y + -height,  1f, 0f)
                    put(x + width, y + height,  1f, 1f)
                    put(x + -width, y + height,  0f, 1f)
                }
            },
            vertexInputs,
            GL_STATIC_DRAW,
            true
        )
    }

    /** Shader just repeats all texture */
    class TextureRenderer(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 1f,
        height: Float = 1f,
        aPositionName: String = "aPosition",
        aUVName: String = "aUV"
    ) : ScreenQuad(x, y, width, height, aPositionName, aUVName) {

        @Language("GLSL")
        val shader = Shader(
                vertCode = """
attribute vec2 $aPositionName;
attribute vec2 $aUVName;

uniform vec2 translation;
uniform vec2 scale;
varying vec2 uv;

void main() {
    uv = $aUVName;
    gl_Position = vec4(translation.x + $aPositionName.x * scale.x, translation.y + $aPositionName.y * scale.y, 0.0, 1.0);
}""",
                fragCode = """            
varying vec2 uv;
uniform sampler2D tex;

void main() {
    gl_FragColor = texture2D(tex, uv);
}""")

        var textureUnit = GL.grabTextureUnit()
            set(value) {
                field = value
                shader["tex"] = value
            }

        init {
            shader.bind()
            setPosition(0f, 0f)
            setScale(1f, 1f)

            shader["tex"] = textureUnit
        }

        fun setPosition(x: Float, y: Float) {
            shader.bind()
            shader.set("translation", x, y)
        }

        fun setScale(x: Float, y: Float) {
            shader.bind()
            shader.set("scale", x, y)
        }

        fun render(
            texture: ITexture,
            out: IFrameBuffer? = null,
            clearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT,
            set: TextureRenderer.() -> Unit = {}
        ) {
            texture.bind(textureUnit)
            render(shader, out, clearMask, set as IScreenQuad.() -> Unit)
        }
    }
}