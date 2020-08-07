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

import org.ksdfv.thelema.g3d.Armature
import org.ksdfv.thelema.g3d.IArmature
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.math.IMat4

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-skin)
 *
 * @author zeganstyl */
class GLTFSkin(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var skin: IArmature = Armature()
): IJsonObjectIO, IGLTFArrayElement {
    override var name: String = ""
    var skeleton: Int = -1
    var inverseBindMatrices: Int = -1

    override fun read(json: IJsonObject) {
        name = json.string("name", "")
        skin.name = name

        val jointsSize = json.array("joints").size

        skin.initBones(jointsSize)

        if (gltf.conf.setupVelocityShader) {
            skin.previousBoneMatrices = Array(jointsSize) { IMat4.Build() }
        }

        json.array("joints") {
            var i = 0
            ints { boneIndex ->
                val node = gltf.nodes[boneIndex]
                skin.bones[i] = node.node
                skin.boneNames[i] = node.name
                i++
            }
        }

        skeleton = json.int("skeleton", -1)
        if (skeleton >= 0) gltf.nodes.getOrWait(skeleton) { node -> skin.node = node.node }

        inverseBindMatrices = json.int("inverseBindMatrices", -1)
        if (inverseBindMatrices >= 0) {
            val ibmAccessor = gltf.accessors[inverseBindMatrices]
            val ibmBufferView = gltf.bufferViews[ibmAccessor.bufferView]

            gltf.buffers.getOrWait(ibmBufferView.buffer) { buffer ->
                buffer.bytes.position = ibmBufferView.byteOffset + ibmAccessor.byteOffset
                val floatBuffer = buffer.bytes.floatView()

                val matrices = skin.inverseBoneMatrices
                for (i in matrices.indices) {
                    floatBuffer.get(matrices[i].values)
                }

                gltf.skins.ready(elementIndex)
            }
        }
    }

    override fun write(json: IJsonObject) {
        if (skin.name.isNotEmpty()) json["name"] = skin.name
        if (skeleton >= 0) json["skeleton"] = skeleton

        json.setArray("joints") {
            val bones = skin.bones
            for (j in bones.indices) {
                val bone = bones[j]
                val joint = gltf.nodes.indexOfFirst { it.node == bone }
                if (joint >= 0) add(j)
            }
        }

        if (inverseBindMatrices >= 0) {
            json["inverseBindMatrices"] = inverseBindMatrices

            val ibmAccessor = gltf.accessors[inverseBindMatrices]
            val ibmBufferView = gltf.bufferViews[ibmAccessor.bufferView]

            val buffer = gltf.buffers[ibmBufferView.buffer]

            buffer.bytes.position = ibmBufferView.byteOffset + ibmAccessor.byteOffset
            val floatBuffer = buffer.bytes.floatView()

            val matrices = skin.inverseBoneMatrices
            for (i in matrices.indices) {
                floatBuffer.put(matrices[i].values)
            }
        }
    }

    override fun destroy() {}
}