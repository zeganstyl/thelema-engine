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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.gl.GL_BACK
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.Vec4
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
open class Material(
    override var shader: IShader? = null,

    override var baseColor: IVec4 = Vec4(1f),
    override var metallic: Float = 0f,
    override var roughness: Float = 1f,

    override var alphaCutoff: Float = 0.5f,
    override var alphaMode: Int = Blending.Clip,
    override var cullFaceMode: Int = GL_BACK,

    override var translucentPriority: Int = 0,

    override var depthMask: Boolean = true,

    override var name: String = "",

    override val shaderChannels: MutableMap<Int, IShader> = HashMap()
): IMaterial {
    override fun copy(): IMaterial = Material().set(this)
}