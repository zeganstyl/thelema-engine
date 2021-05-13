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
import app.thelema.g3d.node.ITransformNode
import app.thelema.json.IJsonObject
import app.thelema.math.IMat4
import app.thelema.math.Mat4
import app.thelema.res.RES
import app.thelema.utils.LOG

/** @author zeganstyl */
class Armature: IArmature {
    override var entityOrNull: IEntity? = null

    private val inverseBindMatricesInternal = ArrayList<IMat4>()
    override val inverseBindMatrices: List<IMat4>
        get() = inverseBindMatricesInternal

    private val boneMatricesUpdateRequests = ArrayList<Boolean>()
    override var boneMatrices: FloatArray = FloatArray(0)
    override var previousBoneMatrices: FloatArray = FloatArray(0)

    private val bonesInternal = ArrayList<ITransformNode?>()
    override val bones: List<ITransformNode?>
        get() = bonesInternal

    override val componentName: String
        get() = Name

    val tmp = Mat4()

    override fun initBones(bonesNum: Int) {
        val oldMatrices = Array(inverseBindMatricesInternal.size) { inverseBindMatricesInternal[it] }
        inverseBindMatricesInternal.clear()
        for (i in 0 until bonesNum) {
            inverseBindMatricesInternal.add(oldMatrices.getOrNull(i) ?: Mat4())
        }
        inverseBindMatricesInternal.trimToSize()

        boneMatrices = FloatArray(bonesNum * 16)

        boneMatricesUpdateRequests.clear()
        for (i in 0 until bonesNum) {
            boneMatricesUpdateRequests.add(true)
        }

        val oldBones = Array(bonesInternal.size) { bonesInternal[it] }
        bonesInternal.clear()
        for (i in 0 until bonesNum) {
            bonesInternal.add(oldBones.getOrNull(i))
        }
        bonesInternal.trimToSize()
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other.componentName == componentName && other != this) {
            other as IArmature

            inverseBindMatricesInternal.clear()
            for (i in other.bones.indices) {
                inverseBindMatricesInternal.add(other.inverseBindMatrices[i])
            }
            inverseBindMatricesInternal.trimToSize()

            boneMatrices = FloatArray(other.boneMatrices.size) { other.boneMatrices[it] }

            boneMatricesUpdateRequests.clear()
            for (i in other.boneMatrices.indices) {
                boneMatricesUpdateRequests.add(true)
            }

            bonesInternal.clear()
            for (i in other.bones.indices) {
                bonesInternal.add(null)
            }
            bonesInternal.trimToSize()

            for (i in other.bones.indices) {
                val it = other.bones[i]
                val path = other.entity.getRelativePathTo(it!!.entity)
                val node = entity.getEntityByPath(path)?.getComponentOrNull<ITransformNode>()
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

        bonesInternal.clear()
        json.array("bones") {
            for (i in 0 until size) {
                bonesInternal.add(null)
            }
            bonesInternal.trimToSize()

            for (i in 0 until size) {
                RES.onComponentAdded(string(i), ITransformNode.Name) {
                    setBone(i, it as ITransformNode?)
                }
            }
        }

        inverseBindMatricesInternal.clear()
        json.array("inverseBindMatrices") {
            objs {
                val mat = Mat4()
                mat.readJson(this)
                inverseBindMatricesInternal.add(mat)
            }
            inverseBindMatricesInternal.trimToSize()
        }

        boneMatrices = FloatArray(bonesInternal.size * 16)

        boneMatricesUpdateRequests.clear()
        for (i in bonesInternal.indices) {
            boneMatricesUpdateRequests.add(true)
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
    }

    override fun setBone(index: Int, bone: ITransformNode?) {
        bonesInternal[index] = bone
    }

    override fun setInverseBindMatrix(index: Int, matrix: IMat4) {
        inverseBindMatricesInternal[index] = matrix
    }

    override fun preUpdateBoneMatrices() {
        for (i in bonesInternal.indices) {
            boneMatricesUpdateRequests[i] = bonesInternal[i]?.isTransformUpdateRequested ?: false
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
    }

    override fun updatePreviousTransform() {
        if (previousBoneMatrices.isNotEmpty()) {
            for (i in previousBoneMatrices.indices) {
                previousBoneMatrices[i] = boneMatrices[i]
            }
        }
    }

    override fun updateBoneMatrices() {
        var floatIndex = 0
        for (i in bones.indices) {
            if (boneMatricesUpdateRequests[i]) {
                val bone = bones[i]
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

    companion object {
        const val Name = "Armature"
    }
}