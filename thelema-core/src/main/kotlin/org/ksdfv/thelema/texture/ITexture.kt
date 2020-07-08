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

package org.ksdfv.thelema.texture

import org.ksdfv.thelema.gl.GL

/**
 * @author zeganstyl
 */
interface ITexture {
    var name: String

    /** The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D. */
    var glTarget: Int

    /** The OpenGL handle for this texture. You can specify for example 0 if don't want to call opengl function */
    var glHandle: Int

    /** Texture filter minification. By default is GL_NEAREST_MIPMAP_LINEAR.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexParameter.xml) */
    var minFilter: Int

    /** Texture filter magnification. By default is GL_LINEAR.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexParameter.xml) */
    var magFilter: Int

    /** used for horizontal (U) texture coordinates. By default is GL_REPEAT.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexParameter.xml) */
    var sWrap: Int

    /** used for vertical (V) texture coordinates. By default is GL_REPEAT.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexParameter.xml) */
    var tWrap: Int

    var rWrap: Int

    /** The currently set anisotropic filtering level for the texture, or 1.0f if none has been set. */
    var anisotropicFilter: Float

    val width: Int
    val height: Int
    val depth: Int

    /** Binds this texture. The texture will be bound to the currently active texture unit. */
    fun bind() {
        GL.glBindTexture(glTarget, glHandle)
    }

    /** Sets active texture unit and binds texture to the given unit.
     * @param unit the unit (0 to MAX_TEXTURE_UNITS).
     */
    fun bind(unit: Int) {
        GL.activeTextureUnit = unit
        GL.glBindTexture(glTarget, glHandle)
    }

    fun destroy() {
        if (glHandle != 0) {
            GL.glDeleteTexture(glHandle)
            glHandle = 0
        }
    }
}