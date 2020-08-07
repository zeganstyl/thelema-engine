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

import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-bufferview)
 *
 * @author zeganstyl */
class GLTFBufferView(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var buffer: Int = -1,
    var byteLength: Int = -1,
    var byteOffset: Int = 0,
    var byteStride: Int = -1,
    var target: Int = -1,
    override var name: String = ""
) : IJsonObjectIO, IGLTFArrayElement {
    override fun read(json: IJsonObject) {
        buffer = json.int("buffer")
        byteLength = json.int("byteLength")
        json.int("byteOffset") { byteOffset = it }
        json.int("byteStride") { byteStride = it }
        json.int("target") { target = it }

        gltf.bufferViews.ready(elementIndex)
    }

    override fun write(json: IJsonObject) {
        json["buffer"] = buffer
        json["byteLength"] = byteLength
        if (byteOffset > 0) json["byteOffset"] = byteOffset
        if (byteStride >= 4) json["byteStride"] = byteStride
        if (target > -1) json["target"] = target
    }

    override fun destroy() {}
}
