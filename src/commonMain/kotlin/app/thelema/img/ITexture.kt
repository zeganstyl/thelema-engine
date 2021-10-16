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

package app.thelema.img

import app.thelema.ecs.IEntityComponent
import app.thelema.gl.GL

/**
 * @author zeganstyl
 */
interface ITexture: IEntityComponent {
    /** The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D. */
    var glTarget: Int

    /** The OpenGL handle for this texture. You can specify for example 0 if don't want to call opengl function */
    var textureHandle: Int

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

    /** If texture handle is not generated, texture will be initiated with one pixel */
    var initOnBind: Boolean

    /** Init texture with default data */
    fun initTexture(color: Int = -1)

    /** Binds this texture. The texture will be bound to the currently active texture unit. */
    fun bind() {
        if (textureHandle == 0 && initOnBind) initTexture()
        GL.glBindTexture(glTarget, textureHandle)
    }

    fun unbind() {
        GL.glBindTexture(glTarget, 0)
    }

    /** Sets active texture unit and binds texture to the given unit.
     * @param unit check size of [GL.textureUnits] to know how many units you can use.
     */
    fun bind(unit: Int) {
        if (textureHandle == 0 && initOnBind) initTexture()
        GL.activeTexture = unit
        GL.glBindTexture(glTarget, textureHandle)
    }

    /** Texture must be bound */
    fun generateMipmapsGPU() {
        if (GL.isGLES) {
            GL.glGenerateMipmap(glTarget)
        } else {
            if (GL.isExtensionSupported("GL_ARB_framebuffer_object") || GL.isExtensionSupported("GL_EXT_framebuffer_object")) {
                GL.glGenerateMipmap(glTarget)
            } else {
                throw RuntimeException("Can't create mipmaps on GPU")
            }
        }
    }

    override fun destroy() {
        if (textureHandle != 0) {
            GL.glDeleteTexture(textureHandle)
            textureHandle = 0
        }
        super.destroy()
    }
}
