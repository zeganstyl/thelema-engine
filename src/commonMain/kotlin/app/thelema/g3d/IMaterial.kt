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

package app.thelema.g3d

import app.thelema.ecs.IEntityComponent
import app.thelema.math.IVec4
import app.thelema.shader.IShader

/** @author zeganstyl */
interface IMaterial: IEntityComponent {
    var alphaCutoff: Float

    /** Use [Blending] */
    var alphaMode: String

    /** The higher value the later the object will be rendered */
    var translucentPriority: Int

    /** Enable or disable writing to depth buffer. By default must be true */
    var depthMask: Boolean

    /** May be GL_FRONT, GL_BACK, GL_FRONT_AND_BACK.
     * If any other value, culling will be disabled.
     * You may set it to zero if you want to display front and back faces (double sided) */
    var cullFaceMode: Int

    /** Default shader */
    var shader: IShader?

    var baseColor: IVec4

    var metallic: Float

    var roughness: Float

    /** Channels may be used to store additional shaders, like velocity and etc */
    val shaderChannels: MutableMap<String, IShader>

    fun set(other: IMaterial): IMaterial {
        alphaCutoff = other.alphaCutoff
        cullFaceMode = other.cullFaceMode
        alphaMode = other.alphaMode
        depthMask = other.depthMask
        translucentPriority = other.translucentPriority
        metallic = other.metallic
        roughness = other.roughness
        baseColor.set(other.baseColor)
        return this
    }

    fun clear() {
        shaderChannels.clear()
    }
}