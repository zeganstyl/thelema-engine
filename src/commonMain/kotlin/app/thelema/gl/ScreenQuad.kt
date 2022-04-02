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
import app.thelema.img.renderNoClear
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.post.PostShader
import app.thelema.shader.useShader
import kotlin.math.ceil
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

/** Default screen quad, that may be used for post processing shaders.
 *
 * Attribute names: POSITION - vertex position, UV - texture coordinates
 *
 * @author zeganstyl */
@ThreadLocal
object ScreenQuad {
    var defaultClearMask: Int? = GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT

    val Layout = VertexLayout()

    val POSITION = Layout.define("POSITION", 2)
    val UV = Layout.define("UV", 2)

    val mesh: IMesh by lazy {
        Mesh {
            primitiveType = GL_TRIANGLE_FAN

            addVertexBuffer(4, POSITION, UV) {
                putFloats(-1f, -1f,  0f, 0f)
                putFloats(1f, -1f,  1f, 0f)
                putFloats(1f, 1f,  1f, 1f)
                putFloats(-1f, 1f,  0f, 1f)
            }
        }
    }

    val textureRenderShader = PostShader(
        flipY = false,
        fragCode = """            
in vec2 uv;
out vec4 FragColor;
uniform sampler2D tex;

void main() {
    FragColor = texture(tex, uv);
}"""
    ).also {
        it.depthMask = false
    }

    val flippedTextureRenderShader = PostShader(
        flipY = true,
        fragCode = """            
in vec2 uv;
out vec4 FragColor;
uniform sampler2D tex;

void main() {
    FragColor = texture(tex, uv);
}"""
    ).also {
        it.depthMask = false
    }

    val texturesRenderShader = Shader(
        vertCode = """
in vec2 POSITION;
in vec2 UV;

uniform vec2 translation;
uniform vec2 scale;
out vec2 uv;

void main() {
    uv = UV;
    gl_Position = vec4(translation + POSITION * scale, 0.0, 1.0);
}""",
        fragCode = """            
in vec2 uv;
out vec4 FragColor;
uniform sampler2D tex;
uniform bool flipY;
uniform vec2 uvScale;

void main() {
    vec2 uv2 = uv;
    if (flipY) uv2.y = 1.0 - uv2.y;
    FragColor = texture(tex, uv2);
    //FragColor = vec4(1.0);
}"""
    ).also {
        it.depthMask = false
        it.vertexLayout = Layout
    }

    fun render(
        texture: ITexture,
        flipY: Boolean = true,
        out: IFrameBuffer? = null,
        clearMask: Int? = defaultClearMask
    ) {
        texture.bind(0)
        render(if (flipY) flippedTextureRenderShader else textureRenderShader, out, clearMask)
    }

    inline fun render(
        numTextures: Int,
        maxCellsInRow: Int = 4,
        maxCellSize: Float = 1f,
        padding: Float = 0f,
        flipY: Boolean = true,
        out: IFrameBuffer? = null,
        getTexture: (i: Int) -> ITexture
    ) {
        val inRow = min(maxCellsInRow, numTextures)
        val rows = ceil(numTextures.toFloat() / inRow).toInt()
        val cell = min(1f / inRow, maxCellSize)
        val size = cell - padding * 2f
        val xOffset = cell - 1f
        val yOffset = cell * rows - 1f + cell
        val step = cell * 2f

        texturesRenderShader.bind()
        texturesRenderShader["flipY"] = flipY
        texturesRenderShader.set("scale", size, size)

        var i = 0
        var y = yOffset
        for (yi in 0 until rows) {
            var x = xOffset
            for (xi in 0 until inRow) {
                if (i >= numTextures) break
                getTexture(i).bind(0)
                texturesRenderShader.set("translation", x, y)
                render(texturesRenderShader, out, null)
                x += step
                i++
            }
            y -= step
        }
    }

    /** Render 2D texture on screen */
    fun render(
        textureHandle: Int,
        flipY: Boolean = true,
        out: IFrameBuffer? = null,
        clearMask: Int? = defaultClearMask
    ) {
        GL.activeTexture = 0
        GL.glBindTexture(GL_TEXTURE_2D, textureHandle)
        render(if (flipY) flippedTextureRenderShader else textureRenderShader, out, clearMask)
    }

    /**
     * Render screen quad mesh with shader
     * @param clearMask if null, glClear will not be called
     * @param set may be used to update shader uniforms. Called after shader bound, so you may not to bind it again.
     * */
    fun render(
        shader: IShader,
        out: IFrameBuffer? = null,
        clearMask: Int? = defaultClearMask
    ) {
        shader.useShader {
            val isDepthTestEnabled = GL.isDepthTestEnabled
            GL.isDepthTestEnabled = false

            if (out != null) {
                out.renderNoClear {
                    if (clearMask != null) GL.glClear(clearMask)
                    mesh.render()
                }
            } else {
                if (clearMask != null) GL.glClear(clearMask)
                mesh.render()
            }

            GL.isDepthTestEnabled = isDepthTestEnabled
        }
    }
}