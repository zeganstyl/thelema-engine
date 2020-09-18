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

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-sparse)
 *
 * @author zeganstyl */
class GLTFSparse(var sparseCount: Int = 0) : IJsonObjectIO {
    val indices = Indices()
    val values = Values()

    override fun read(json: IJsonObject) {
        sparseCount = json.int("count")
        json.get("indices") { indices.read(this) }
        json.get("values") { values.read(this) }
    }

    override fun write(json: IJsonObject) {
        json["sparseCount"] = sparseCount
        json["indices"] = indices
        json["values"] = values
    }

    /** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-indices) */
    class Indices(var bufferView: Int = -1, var byteOffset: Int = 0, var componentType: Int = -1) :
        IJsonObjectIO {
        override fun read(json: IJsonObject) {
            bufferView = json.int("bufferView")
            json.int("byteOffset") { byteOffset = it }
            componentType = json.int("componentType")
        }

        override fun write(json: IJsonObject) {
            json["bufferView"] = bufferView
            json["componentType"] = componentType

            if (byteOffset > 0) json["byteOffset"] = byteOffset
        }
    }

    /** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-values) */
    class Values(var bufferView: Int = -1, var byteOffset: Int = 0) :
        IJsonObjectIO {
        override fun read(json: IJsonObject) {
            bufferView = json.int("bufferView")
            json.int("byteOffset") { byteOffset = it }
        }

        override fun write(json: IJsonObject) {
            json["bufferView"] = bufferView
            if (byteOffset > 0) json["byteOffset"] = byteOffset
        }
    }
}