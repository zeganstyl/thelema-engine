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

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.node.ITransformNode
import app.thelema.math.IMat4

/** @author zeganstyl */
interface IArmature: IEntityComponent {
    val inverseBindMatrices: List<IMat4>

    /** Current bones transformations. It must be passed to shader */
    val boneMatrices: FloatArray

    /** May be used for motion blur.
     * If previous transform data is not used, it must be linked to some global empty array. */
    var previousBoneMatrices: FloatArray

    val bones: List<ITransformNode?>

    fun addBone(bone: ITransformNode?, inverseBindMatrix: IMat4)

    fun setBone(index: Int, bone: ITransformNode?)

    fun setInverseBindMatrix(index: Int, matrix: IMat4)

    fun removeBone(bone: ITransformNode)

    fun removeBone(index: Int)

    fun initBones(bonesNum: Int)

    fun updatePreviousTransform()

    fun preUpdateBoneMatrices()

    fun updateBoneMatrices()
}
