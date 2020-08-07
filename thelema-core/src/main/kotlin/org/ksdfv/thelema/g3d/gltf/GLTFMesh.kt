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

/** [glTF 2.0 specification - mesh](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#meshes)
 *
 * @author mgsx, zeganstyl */
class GLTFMesh(
    override val gltf: IGLTF,
    override var elementIndex: Int
): IJsonObjectIO, IGLTFArrayElement {
    override var name: String = ""
    val primitives = GLTFArray<GLTFPrimitive>("primitives")

    override fun read(json: IJsonObject) {
        json.string("name") { name = it }

        json.array("primitives") {
            objs {
                val primitive = GLTFPrimitive(
                    gltf = gltf,
                    elementIndex = primitives.size,
                    meshIndex = elementIndex
                )
                primitives.add(primitive)
                primitive.read(this)
            }
        }

        gltf.meshes.ready(elementIndex)
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name

        json.setArray("primitives") {
            for (i in primitives.indices) {
                add(primitives[i])
            }
        }
    }

    override fun destroy() {}
}
