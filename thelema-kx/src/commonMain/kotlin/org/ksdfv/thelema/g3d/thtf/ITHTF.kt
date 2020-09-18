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

import org.ksdfv.thelema.g3d.gltf.IGLTF
import org.ksdfv.thelema.g3d.gltf.IGLTFArray

/** Thelema extends glTF and puts objects to extra section
 *
 * @author zeganstyl */
interface ITHTF: IGLTF {
    val shaders: IGLTFArray<THTFShader>
    val libraries: IGLTFArray<THTFLibrary>

    override fun traverseArrays(call: (array: IGLTFArray<*>) -> Unit) {
        call(shaders)
        call(libraries)
    }
}