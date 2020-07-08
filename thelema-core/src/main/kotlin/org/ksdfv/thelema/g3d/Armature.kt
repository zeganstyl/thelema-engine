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

/** @author zeganstyl */
class Armature: IArmature {
    override var name: String = ""
    override var node: ITransformNode? = null
    override var inverseBoneMatrices: Array<IMat4> = Array(0) { IMat4.Build() }
    override var boneMatrices: Array<IMat4> = Array(0) { IMat4.Build() }
    override var previousBoneMatrices: Array<IMat4> = IArmature.previousBoneMatricesCap
    override var boneNames = Array(0) { "" }
    override var bones: Array<ITransformNode> = Array(0) { ITransformNode.Cap }

    override fun copy(): IArmature {
        val newArmature = Armature()
        newArmature.name = name
        newArmature.inverseBoneMatrices = Array(inverseBoneMatrices.size) { IMat4.Build().set(inverseBoneMatrices[it]) }
        newArmature.boneMatrices = Array(boneMatrices.size) { IMat4.Build().set(boneMatrices[it]) }
        newArmature.bones = Array(bones.size) { bones[it] }
        newArmature.boneNames = Array(boneNames.size) { boneNames[it] }

        return newArmature
    }
}