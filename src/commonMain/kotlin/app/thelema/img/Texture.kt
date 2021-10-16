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

import app.thelema.gl.*

/**
 * @author zeganstyl
 */
abstract class Texture(override var glTarget: Int): ITexture {
    override var textureHandle: Int = 0

    override var width: Int = 0
    override var height: Int = 0

    override var initOnBind: Boolean = true

    private var updateParamsRequest = false

    override var minFilter: Int = GL_NEAREST_MIPMAP_LINEAR
        set(value) {
            if (value != field) {
                field = value
                updateParamsRequest = true
            }
        }

    override var magFilter: Int = GL_LINEAR
        set(value) {
            if (value != field) {
                field = value
                updateParamsRequest = true
            }
        }

    /** horizontal (u) wrap */
    override var sWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                updateParamsRequest = true
            }
        }

    /** vertical (v) wrap */
    override var tWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                updateParamsRequest = true
            }
        }

    override var rWrap: Int = GL_REPEAT
        set(value) {
            if (value != field) {
                field = value
                updateParamsRequest = true
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
                        updateParamsRequest = true
                    }
                }
            }
        }

    private fun updateParams() {
        if (updateParamsRequest) {
            updateParamsRequest = false
            GL.glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, minFilter)
            GL.glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, magFilter)
            GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, sWrap)
            GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, tWrap)
            GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_R, rWrap)
            if (GL.maxAnisotropicFilterLevel != 1f) GL.glTexParameterf(glTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropicFilter)
        }
    }

    override fun bind(unit: Int) {
        super.bind(unit)
        updateParams()
    }

    override fun bind() {
        super.bind()
        updateParams()
    }
}
