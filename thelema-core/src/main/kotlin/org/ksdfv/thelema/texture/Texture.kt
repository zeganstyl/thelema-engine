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

import org.ksdfv.thelema.gl.*

/**
 * @author zeganstyl
 */
abstract class Texture(
    override var glTarget: Int,
    override var glHandle: Int = GL.glGenTexture()
): ITexture {
    override var name: String = ""

    var index = 0

    override var minFilter: Int = GL_LINEAR
        set(value) {
            if (value != field) {
                field = value
                GL.glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, value)
            }
        }

    override var magFilter: Int = GL_LINEAR
        set(value) {
            if (value != field) {
                field = value
                GL.glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, value)
            }
        }

    /** horizontal (u) wrap */
    override var sWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, value)
            }
        }

    /** vertical (v) wrap */
    override var tWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, value)
            }
        }

    override var rWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_R, value)
            }
        }

    /** Anisotropic filtering level for the texture, or 1.0f if none has been set. */
    override var anisotropicFilter = 1f
        set(value) {
            if (field != value) {
                val max = GL.maxAnisotropicFilterLevel
                if (max == 1f) {
                    field = 1f
                } else {
                    val oldValue = field
                    field = if (value > max) max else value
                    if (field != oldValue) {
                        GL.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, field)
                    }
                }
            }
        }
}
