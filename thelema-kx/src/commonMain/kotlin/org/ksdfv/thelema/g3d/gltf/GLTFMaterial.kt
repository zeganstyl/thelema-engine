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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.g3d.IMaterial
import org.ksdfv.thelema.g3d.Material
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.MATH

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-material)
 *
 * @author zeganstyl */
open class GLTFMaterial(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    override var material: IMaterial = Material()
): IGLTFMaterial {
    override var name: String = ""

    override var alphaMode: String = "OPAQUE"
    override var alphaCutoff: Float = 0.5f
    override var doubleSided = false

    override var baseColorTexture: Int = -1
    override var baseColorTextureUV: Int = 0
    override var baseColor: IVec4? = null
    override var metallic: Float = 0f
    override var roughness: Float = 1f
    override var metallicRoughnessTexture: Int = -1
    override var metallicRoughnessTextureUV: Int = 0

    override var occlusionStrength: Float = 1f
    override var occlusionTexture: Int = -1
    override var occlusionTextureUV: Int = 0

    override var normalScale: Float = 1f
    override var normalTexture: Int = -1
    override var normalTextureUV: Int = 0

    override var emissiveFactor: IVec3 = MATH.Zero3
    override var emissiveTexture: Int = -1
    override var emissiveTextureUV: Int = 0
}
