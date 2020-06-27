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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.Mat4

/** @author zeganstyl */
interface IArmature {
    var name: String

    /** Root node contains bones tree */
    var node: ITransformNode?

    var inverseBoneMatrices: Array<IMat4>

    /** Current bones transformations. It must be passed to shader */
    var boneMatrices: Array<IMat4>

    /** May be used to know which bone on which index must be linked. */
    var boneNames: Array<String>

    var bones: Array<ITransformNode>

    fun initBones(bonesNum: Int, saveLinks: Boolean = true) {
        if (saveLinks) {
            val inverseBoneMatricesOld = inverseBoneMatrices
            val boneMatricesOld = boneMatrices
            val boneNamesOld = boneNames
            val bonesOld = bones

            inverseBoneMatrices = Array(bonesNum) { if (it < inverseBoneMatricesOld.size) inverseBoneMatricesOld[it] else Mat4() }
            boneMatrices = Array(bonesNum) { if (it < boneMatricesOld.size) boneMatricesOld[it] else Mat4() }
            boneNames = Array(bonesNum) { if (it < boneNamesOld.size) boneNamesOld[it] else "" }
            bones = Array(bonesNum) { if (it < bonesOld.size) bonesOld[it] else ITransformNode.Default }
        } else {
            inverseBoneMatrices = Array(bonesNum) { Mat4() }
            boneMatrices = Array(bonesNum) { Mat4() }
            boneNames = Array(bonesNum) { "" }
            bones = Array(bonesNum) { ITransformNode.Default }
        }
    }

    fun update(delta: Float) {
        for (i in boneMatrices.indices) {
            bones[i].getWorldMatrix(boneMatrices[i]).mul(inverseBoneMatrices[i])
        }
    }

    /** New bones will be linked to array and to animations. Bones are linking by names from [boneNames]
     * @param unlinkCurrent if true, old bones will be replaced with [ITransformNode.Default] */
    fun linkBones(newBones: List<ITransformNode>, unlinkCurrent: Boolean = true) {
        val bones = bones

        if (unlinkCurrent) {
            for (i in bones.indices) {
                bones[i] = ITransformNode.Default
            }
        }

        val boneNames = boneNames
        for (i in boneNames.indices) {
            val boneName = boneNames[i]
            val newBone = newBones.firstOrNull { it.name == boneName }
            if (newBone != null) {
                bones[i] = newBone
            }
        }
    }

    fun copy(): IArmature

    fun clear() {
        initBones(0)
    }
}
