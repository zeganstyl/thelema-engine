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

package app.thelema.g3d.node

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.getComponentOrNull
import app.thelema.math.*

/** Spatial transform node. Node may contain children and forms a tree of nodes.
 * Node may contain any transform data, depended on [nodeType].
 * Nodes with less data, may be more optimal in calculations and memory consumption.
 * @author zeganstyl */
interface ITransformNode: IEntityComponent {
    /** Local translation, relative to the parent. */
    val position: IVec3
        get() = transformData.position

    /** Local rotation, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val rotation: IVec4
        get() = transformData.rotation

    /** Local scale, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val scale: IVec3
        get() = transformData.scale

    /** Global world transform, product of multiply local and parent transform data, calculated via [updateTransform]. */
    val worldMatrix: IMat4
        get() = transformData.worldMatrix

    var transformData: ITransformData

    var isTransformUpdateRequested: Boolean
    
    fun requestTransformUpdate(recursive: Boolean = true) {
        isTransformUpdateRequested = true
        if (recursive) {
            entityOrNull?.forEachEntityInBranch {
                it.getComponentOrNull<ITransformNode>()?.isTransformUpdateRequested = true
            }
        }
    }

    fun updateTransform()

    fun reset() {
        position.set(0f, 0f, 0f)
        rotation.set(0f, 0f, 0f, 1f)
        scale.set(1f, 1f, 1f)
        worldMatrix.idt()
        updateTransform()
    }

    fun getGlobalPosition(out: IVec3): IVec3 = worldMatrix.getCol3Vec3(out)

    companion object {
        const val Name = "TransformNode"
    }
}