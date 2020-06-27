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

import org.ksdfv.thelema.IFrameBuffer
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_TRIANGLE_FAN
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
open class ScreenQuad(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 1f,
        height: Float = 1f): IScreenQuad {
    override val mesh: IMesh = IMesh.Build().apply {
        primitiveType = GL_TRIANGLE_FAN
        vertices = IVertexBuffer.build(
            DATA.bytes(20 * 4).apply {
                floatView().apply {
                    // x, y, z,   u, v
                    put(x + -width, y + -height, 0f, 0f, 0f)
                    put(x + width, y + -height, 0f, 1f, 0f)
                    put(x + width, y + height, 0f, 1f, 1f)
                    put(x + -width, y + height, 0f, 0f, 1f)
                }
            },
            VertexAttributes(VertexAttribute.Position, VertexAttribute.UV[0])
        )
    }

    /** Shader just repeats all texture */
    class TextureRenderer(
            x: Float = 0f,
            y: Float = 0f,
            width: Float = 1f,
            height: Float = 1f
    ) : ScreenQuad(x, y, width, height) {
        val shader = Shader(
                vertCode = RenderTextureOnScreenPartVertexShader,
                fragCode = Shader.RenderTextureFragmentShader)
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

        companion object {
            val RenderTextureOnScreenPartVertexShader = """
                attribute vec4 a_position;
                attribute vec2 a_texCoord0;
                
                uniform vec2 translation;
                uniform vec2 scale;
                varying vec2 v_texCoords;
                
                void main() {
                    v_texCoords = a_texCoord0;
                    gl_Position = vec4(translation.x + a_position.x * scale.x, translation.y + a_position.y * scale.y, a_position.z, a_position.w);
                }
            """.trimIndent()
        }
    }
}