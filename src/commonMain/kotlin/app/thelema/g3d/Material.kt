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

import app.thelema.ecs.IEntity
import app.thelema.gl.GL_BACK
import app.thelema.math.IVec4
import app.thelema.math.Vec4
import app.thelema.shader.IShader

/** @author zeganstyl */
class Material: IMaterial {
    override var entityOrNull: IEntity? = null

    override val shaderChannels: MutableMap<String, IShader> = HashMap()

    override var shader: IShader?
        get() = shaderChannels[ShaderChannel.Default]
        set(value) {
            if (value != null) {
                shaderChannels[ShaderChannel.Default] = value
            } else {
                shaderChannels.remove(ShaderChannel.Default)
            }
        }

    override var baseColor: IVec4 = Vec4(1f)
    override var metallic: Float = 0f
    override var roughness: Float = 1f

    override var alphaCutoff: Float = 0.5f
    override var alphaMode: String = Blending.MASK
    override var cullFaceMode: Int = GL_BACK

    override var translucentPriority: Int = 0

    override var depthMask: Boolean = true

    override val componentName: String
        get() = Name

    companion object {
        const val Name = "Material"
    }
}