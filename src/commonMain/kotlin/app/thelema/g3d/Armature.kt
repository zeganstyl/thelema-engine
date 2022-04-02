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
import app.thelema.gl.ArmatureUniforms
import app.thelema.gl.IUniformBuffer
import app.thelema.gl.UniformBuffer
import app.thelema.json.IJsonObject
import app.thelema.math.IMat4
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/** @author zeganstyl */
class Armature: IArmature {
    override var entityOrNull: IEntity? = null

    private val _inverseBindMatrices = ArrayList<IMat4>(0)
    override val inverseBindMatrices: List<IMat4>
        get() = if (_inverseBindMatrices.size == 0) (inheritedArmature?.inverseBindMatrices ?: _inverseBindMatrices) else _inverseBindMatrices

    private val bonesIndices = HashMap<ITransformNode, Int>()
    private val _bones = ArrayList<ITransformNode?>()
    override val bones: List<ITransformNode?>
        get() = _bones

    override val componentName: String
        get() = "Armature"

    val tmp = Mat4()

    override var inheritedArmature: IArmature? = null

    override val uniformBuffer: IUniformBuffer = UniformBuffer(ArmatureUniforms)

    private val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            bonesIndices[node]?.also { setMatrixData(node, it) }
        }

        override fun previousWorldMatrixChanged(node: ITransformNode) {
            bonesIndices[node]?.also { setPrevMatrixData(it) }
        }
    }

    private fun setPrevMatrixData(index: Int) {
        val offset = index * 64
        uniformBuffer.bytes.put(
            ArmatureUniforms.PrevBoneMatrices.bytePosition + offset,
            uniformBuffer.bytes,
            ArmatureUniforms.BoneMatrices.bytePosition + offset,
            64
        )
        uniformBuffer.requestBufferUploading()
    }

    private fun setMatrixData(node: ITransformNode, index: Int) {
        tmp.set(node.worldMatrix).mul(inverseBindMatrices[index])

        val bytes = uniformBuffer.bytes
        bytes.position = ArmatureUniforms.BoneMatrices.bytePosition + index * 64
        bytes.putFloatsArray(tmp.values)

        uniformBuffer.requestBufferUploading()
    }

    private fun resetMatrixData(index: Int) {
        val bytes = uniformBuffer.bytes
        bytes.position = ArmatureUniforms.BoneMatrices.bytePosition + index * 64
        bytes.putFloatsArray(MATH.IdentityMat4.values)

        setPrevMatrixData(index)

        uniformBuffer.requestBufferUploading()
    }

    override fun initBones(bonesNum: Int) {
        clearBones()

        val oldIbms = Array(_inverseBindMatrices.size) { _inverseBindMatrices[it] }
        val oldBones = Array(_bones.size) { _bones[it] }

        for (i in 0 until bonesNum) {
            addBone(oldBones.getOrNull(i), oldIbms.getOrNull(i) ?: Mat4())
        }
        _bones.trimToSize()
        _inverseBindMatrices.trimToSize()

        uniformBuffer.bytes.put(
            ArmatureUniforms.PrevBoneMatrices.bytePosition,
            uniformBuffer.bytes,
            ArmatureUniforms.BoneMatrices.bytePosition,
            _bones.size * 64
        )
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other is IArmature && other != this) {
            clearBones()

            for (i in other.bones.indices) {
                addBone(null, other.inverseBindMatrices[i])
            }
            _bones.trimToSize()
            _inverseBindMatrices.trimToSize()

            for (i in other.bones.indices) {
                val it = other.bones[i]
                val path = other.entity.getRelativePathTo(it!!.entity)
                val node = entity.getEntityByPath(path)?.componentOrNull<ITransformNode>()
                setupBone(node, i)
                if (node != null) {
                    _bones[i] = node
                } else {
                    LOG.error("Armature: can't link node ${it.entityOrNull?.name} by path: $path")
                }
            }
        }

        return this
    }

    private fun clearBones() {
        _inverseBindMatrices.clear()
        _bones.iterate { it?.removeListener(nodeListener) }
        _bones.clear()
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        clearBones()
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
        _bones.trimToSize()
        _inverseBindMatrices.trimToSize()

        var i = 0
        json.array("inverseBindMatrices") {
            forEachObject {
                _inverseBindMatrices[i].readJson(this)
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
            for (i in _inverseBindMatrices.indices) {
                add(_inverseBindMatrices[i])
            }
        }
    }

    private fun setupBone(bone: ITransformNode?, index: Int) {
        if (bone != null) {
            setMatrixData(bone, index)
            setPrevMatrixData(index)
            bonesIndices[bone] = index
            bone.addListener(nodeListener)
        } else {
            resetMatrixData(index)
        }
    }

    override fun addBone(bone: ITransformNode?, inverseBindMatrix: IMat4) {
        _inverseBindMatrices.add(inverseBindMatrix)

        setupBone(bone, _bones.size)
        _bones.add(bone)
    }

    override fun setBone(index: Int, bone: ITransformNode?) {
        _bones[index]?.removeListener(nodeListener)
        _bones[index] = bone
        setupBone(bone, index)
    }

    override fun setInverseBindMatrix(index: Int, matrix: IMat4) {
        _inverseBindMatrices[index] = matrix
    }

    override fun removeBone(bone: ITransformNode) {
        val index = _bones.indexOf(bone)
        if (index >= 0) removeBone(index)
    }

    override fun removeBone(index: Int) {
        val bone = _bones.removeAt(index)
        if (bone != null) {
            bone.removeListener(nodeListener)
            bonesIndices.remove(bone)
        }
        _inverseBindMatrices.removeAt(index)
    }
}