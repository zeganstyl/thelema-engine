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

import app.thelema.ecs.*
import app.thelema.json.IJsonObject
import app.thelema.math.IMat4
import app.thelema.math.Mat4
import app.thelema.utils.LOG

/** @author zeganstyl */
class Armature: IArmature {
    override var entityOrNull: IEntity? = null

    private val inverseBindMatricesInternal = ArrayList<IMat4>(0)
    override val inverseBindMatrices: List<IMat4>
        get() = if (inverseBindMatricesInternal.size == 0) (inheritedArmature?.inverseBindMatrices ?: inverseBindMatricesInternal) else inverseBindMatricesInternal

    private val boneMatricesUpdateRequests = ArrayList<Boolean>()
    override var boneMatrices: FloatArray = FloatArray(0)
    override var previousBoneMatrices: FloatArray = FloatArray(0)

    override var isPreviousBoneMatricesEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resizeBoneDataRequest = true
            }
        }

    private val bonesInternal = ArrayList<ITransformNode?>()
    override val bones: List<ITransformNode?>
        get() = bonesInternal

    override val componentName: String
        get() = "Armature"

    val tmp = Mat4()

    override var inheritedArmature: IArmature? = null

    private var resizeBoneDataRequest = true

    private fun setMatrixUpdateRequest(index: Int, value: Boolean) {
        if (index >= boneMatricesUpdateRequests.size) {
            boneMatricesUpdateRequests.add(value)
        } else {
            boneMatricesUpdateRequests[index] = value
        }
    }

    override fun initBones(bonesNum: Int) {
        val oldIbms = Array(inverseBindMatricesInternal.size) { inverseBindMatricesInternal[it] }
        val oldBones = Array(bonesInternal.size) { bonesInternal[it] }

        bonesInternal.clear()
        inverseBindMatricesInternal.clear()
        for (i in 0 until bonesNum) {
            addBone(oldBones.getOrNull(i), oldIbms.getOrNull(i) ?: Mat4())
        }
        bonesInternal.trimToSize()
        inverseBindMatricesInternal.trimToSize()
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other is IArmature && other != this) {
            isPreviousBoneMatricesEnabled = other.isPreviousBoneMatricesEnabled

            boneMatricesUpdateRequests.clear()
            for (i in other.boneMatrices.indices) {
                boneMatricesUpdateRequests.add(true)
            }

            inverseBindMatricesInternal.clear()
            bonesInternal.clear()
            for (i in other.bones.indices) {
                addBone(null, other.inverseBindMatrices[i])
            }
            bonesInternal.trimToSize()
            inverseBindMatricesInternal.trimToSize()

            for (i in other.bones.indices) {
                val it = other.bones[i]
                val path = other.entity.getRelativePathTo(it!!.entity)
                val node = entity.getEntityByPath(path)?.componentOrNull<ITransformNode>()
                if (node != null) {
                    bonesInternal[i] = node
                } else {
                    LOG.error("Armature: can't link node ${it.entityOrNull?.name} by path: $path")
                }
            }
        }

        return this
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        boneMatricesUpdateRequests.clear()
        inverseBindMatricesInternal.clear()
        bonesInternal.clear()
        json.array("bones") {
            for (i in 0 until size) {
                addBone(null, Mat4())
            }

            val e = entityOrNull
            if (e != null) {
                for (i in 0 until size) {
                    setBone(i, e.getRootEntity().makePath(string(i)).component())
                }
            }
        }
        bonesInternal.trimToSize()
        inverseBindMatricesInternal.trimToSize()
        boneMatricesUpdateRequests.trimToSize()

        var i = 0
        json.array("inverseBindMatrices") {
            forEachObject {
                inverseBindMatricesInternal[i].readJson(this)
                i++
            }
        }
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json.setArray("bones") {
            for (i in bones.indices) {
                val node = bones[i]
                if (node != null) add(node.entity.path)
            }
        }

        json.setArray("inverseBindMatrices") {
            for (i in inverseBindMatricesInternal.indices) {
                add(inverseBindMatricesInternal[i])
            }
        }
    }

    override fun addBone(bone: ITransformNode?, inverseBindMatrix: IMat4) {
        bonesInternal.add(bone)
        boneMatricesUpdateRequests.add(true)
        inverseBindMatricesInternal.add(inverseBindMatrix)
        resizeBoneDataRequest = true
    }

    override fun setBone(index: Int, bone: ITransformNode?) {
        bonesInternal[index] = bone
    }

    override fun setInverseBindMatrix(index: Int, matrix: IMat4) {
        inverseBindMatricesInternal[index] = matrix
    }

    override fun preUpdateBoneMatrices() {
        for (i in bonesInternal.indices) {
            setMatrixUpdateRequest(i, bonesInternal[i]?.isTransformUpdateRequested ?: false)
        }
    }

    override fun updatePreviousBoneMatrices() {
        if (previousBoneMatrices.isNotEmpty()) {
            for (i in previousBoneMatrices.indices) {
                previousBoneMatrices[i] = boneMatrices[i]
            }
        }
    }

    override fun removeBone(bone: ITransformNode) {
        val index = bonesInternal.indexOf(bone)
        if (index >= 0) removeBone(index)
    }

    override fun removeBone(index: Int) {
        bonesInternal.removeAt(index)
        inverseBindMatricesInternal.removeAt(index)
        boneMatricesUpdateRequests.removeAt(index)
        resizeBoneDataRequest = true
    }

    override fun updateBoneMatrices() {
        if (resizeBoneDataRequest) {
            resizeBoneDataRequest = false

            val floats = bonesInternal.size * 16
            if (boneMatrices.size != floats) boneMatrices = FloatArray(floats)
            if (isPreviousBoneMatricesEnabled && previousBoneMatrices.size != floats) {
                previousBoneMatrices = FloatArray(floats)
            }
        }

        var floatIndex = 0
        val inverseBindMatrices = inverseBindMatrices
        for (i in bonesInternal.indices) {
            if (boneMatricesUpdateRequests[i]) {
                val bone = bonesInternal[i]
                if (bone != null) {
                    tmp.set(bone.worldMatrix).mul(inverseBindMatrices[i])
                } else {
                    tmp.idt()
                }

                for (j in 0 until 16) {
                    boneMatrices[floatIndex] = tmp.values[j]
                    floatIndex++
                }
            }
        }
    }
}