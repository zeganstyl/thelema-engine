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

package org.ksdfv.thelema.g3d.thtf

import org.ksdfv.thelema.g3d.IMaterial
import org.ksdfv.thelema.g3d.Material
import org.ksdfv.thelema.g3d.gltf.GLTFMaterial
import org.ksdfv.thelema.g3d.gltf.IGLTF
import org.ksdfv.thelema.json.IJsonObject

/** @author zeganstyl */
class THTFMaterial(
    override val thtf: ITHTF,
    elementIndex: Int,
    material: IMaterial = Material()
): GLTFMaterial(thtf, elementIndex, material), ITHTFArrayElement {
    override val gltf: IGLTF
        get() = super<ITHTFArrayElement>.gltf

    override fun read(json: IJsonObject) {
        super.read(json)

        val shaderIndex = thtf.shaders.size
        val thtfShader = THTFShader(thtf, shaderIndex)
        thtf.shaders.add(thtfShader)
        thtfShader.assembleFrom(material.shader!!)
    }
}