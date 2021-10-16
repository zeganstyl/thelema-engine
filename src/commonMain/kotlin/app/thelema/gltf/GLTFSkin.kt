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

package app.thelema.gltf

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.IArmature
import app.thelema.g3d.ITransformNode
import app.thelema.math.IMat4
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.TransformDataType

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-skin)
 *
 * @author zeganstyl */
class GLTFSkin(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var skeleton: Int = -1
    var inverseBindMatricesIndex: Int = -1

    val joints = ArrayList<Int>()

    override val defaultName: String
        get() = "Armature"

    var inverseBindMatrices: Array<IMat4> = emptyArray()

    fun writeEntity(entity: IEntity, nodes: Map<Int, ITransformNode>) {
        val skin = entity.component<IArmature>()

        skin.initBones(joints.size)

        skin.isPreviousBoneMatricesEnabled = gltf.conf.setupVelocityShader

        for (i in joints.indices) {
            val node = nodes[joints[i]]!!
            skin.setBone(i, node)
        }

        if (inverseBindMatricesIndex >= 0) {
            for (i in inverseBindMatrices.indices) {
                skin.setInverseBindMatrix(i, inverseBindMatrices[i])
            }
        }
    }

    override fun readJson() {
        super.readJson()

        joints.clear()
        json.array("joints") {
            forEachInt { joints.add(it) }
        }

        skeleton = json.int("skeleton", -1)

        inverseBindMatricesIndex = json.int("inverseBindMatrices", -1)
        if (inverseBindMatricesIndex >= 0) {
            val ibmAccessor = gltf.accessors[inverseBindMatricesIndex] as GLTFAccessor
            val ibmBufferView = gltf.bufferViews[ibmAccessor.bufferView] as GLTFBufferView

            gltf.buffers.getOrWait(ibmBufferView.buffer) {
                it as GLTFBuffer

                it.bytes.limit = ibmBufferView.byteOffset + ibmBufferView.byteLength
                it.bytes.position = ibmBufferView.byteOffset + ibmAccessor.byteOffset

                inverseBindMatrices = Array(ibmAccessor.count) { Mat4() }

                for (i in inverseBindMatrices.indices) {
                    val mat = inverseBindMatrices[i]
                    it.bytes.getFloats(mat.values)

                    // optimize performance if possible
                    if (
                        MATH.isEqual(mat.m00, 1f) &&
                        MATH.isEqual(mat.m01, 0f) &&
                        MATH.isEqual(mat.m02, 0f) &&

                        MATH.isEqual(mat.m10, 0f) &&
                        MATH.isEqual(mat.m11, 1f) &&
                        MATH.isEqual(mat.m12, 0f) &&

                        MATH.isEqual(mat.m20, 0f) &&
                        MATH.isEqual(mat.m21, 0f) &&
                        MATH.isEqual(mat.m22, 1f) &&

                        MATH.isEqual(mat.m30, 0f) &&
                        MATH.isEqual(mat.m31, 0f) &&
                        MATH.isEqual(mat.m32, 0f) &&
                        MATH.isEqual(mat.m33, 1f)
                    ) mat.transformDataType = TransformDataType.Translation
                }

                ready()
            }
        } else {
            ready()
        }
    }

    override fun writeJson() {
        super.writeJson()
        if (skeleton >= 0) json["skeleton"] = skeleton

        json.setArray("joints") {
            for (i in joints.indices) {
                add(joints[i])
            }
        }

        if (inverseBindMatricesIndex >= 0) {
            json["inverseBindMatrices"] = inverseBindMatricesIndex

//            val ibmAccessor = gltf.accessors[inverseBindMatricesIndex] as GLTFAccessor
//            val ibmBufferView = gltf.bufferViews[ibmAccessor.bufferView] as GLTFBufferView
//
//            val buffer = gltf.buffers[ibmBufferView.buffer] as GLTFBuffer
//
//            buffer.bytes.position = ibmBufferView.byteOffset + ibmAccessor.byteOffset
//            val floatBuffer = buffer.bytes.floatView()
//
//            val matrices = skin.inverseBindMatrices
//            for (i in matrices.indices) {
//                floatBuffer.put(matrices[i].values)
//            }
        }
    }

    override fun destroy() {}
}