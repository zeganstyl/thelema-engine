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

import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-sampler)
 * @author zeganstyl */
class GLTFSampler(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var minFilter: Int = GL_LINEAR,
    var magFilter: Int = GL_LINEAR,
    var wrapS: Int = GL_REPEAT,
    var wrapT: Int = GL_REPEAT,
    override var name: String = ""
) : IJsonObjectIO, IGLTFArrayElement {
    override fun read(json: IJsonObject) {
        json.int("minFilter") { minFilter = it }
        json.int("magFilter") { magFilter = it }
        json.int("wrapS") { wrapS = it }
        json.int("wrapT") { wrapT = it }
    }

    override fun write(json: IJsonObject) {
        if (magFilter == GL_LINEAR || magFilter == GL_NEAREST) {
            json["magFilter"] = magFilter
        }

        when (minFilter) {
            GL_LINEAR,
            GL_NEAREST,
            GL_NEAREST_MIPMAP_NEAREST,
            GL_LINEAR_MIPMAP_NEAREST,
            GL_NEAREST_MIPMAP_LINEAR,
            GL_LINEAR_MIPMAP_LINEAR -> json["minFilter"] = minFilter
        }

        if (wrapS != GL_REPEAT) json["wrapS"] = wrapS
        if (wrapT != GL_REPEAT) json["wrapT"] = wrapT
    }

    override fun destroy() {}
}