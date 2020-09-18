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

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_FLOAT
import org.ksdfv.thelema.gl.GL_TRIANGLE_FAN
import org.ksdfv.thelema.img.IFrameBuffer
import org.ksdfv.thelema.img.ITexture
import org.ksdfv.thelema.shader.Shader

/** @author zeganstyl */
open class ScreenQuad(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 1f,
    height: Float = 1f,
    positionName: String = "POSITION",
    uvName: String = "UV"
): IScreenQuad {
    override val mesh: IMesh = MESH.mesh().apply {
        primitiveType = GL_TRIANGLE_FAN

        val vertexInputs = VertexInputs(
            VertexInput(2, positionName, GL_FLOAT, true),
            VertexInput(2, uvName, GL_FLOAT, true)
        )

        vertices = MESH.vertexBuffer(
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
            initGpuObjects = true
        )
    }

    /** Shader renders whole texture as is. May be used for debugging */
    class TextureRenderer(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 1f,
        height: Float = 1f,
        positionName: String = "POSITION",
        uvName: String = "UV"
    ) : ScreenQuad(x, y, width, height, positionName, uvName) {

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
    gl_FragColor = texture2D(tex, uv);
}""")
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

        fun render(
            texture: ITexture,
            out: IFrameBuffer? = null,
            clearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT,
            set: TextureRenderer.() -> Unit = {}
        ) {
            texture.bind(0)
            render(shader, out, clearMask) { set() }
        }
    }
}