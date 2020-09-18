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

package org.ksdfv.thelema.g3d.node

import org.ksdfv.thelema.ext.traverseSafe
import org.ksdfv.thelema.g3d.G3D
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.math.Vec4

/** Translation (position), rotation, scale node (TRS). It is default node.
 * @author zeganstyl */
class Node: ITransformNode {
    override var name: String = ""

    override val position: IVec3 = Vec3()
    override val rotation = Vec4()
    override val scale: IVec3 = Vec3(1f, 1f, 1f)

    override val worldMatrix = Mat4()

    override var parentNode: ITransformNode? = null

    override val childNodes = ArrayList<ITransformNode>()

    override fun addChildNode(node: ITransformNode) {
        super.addChildNode(node)
        childNodes.add(node)
    }

    override fun removeChildNode(node: ITransformNode) {
        super.removeChildNode(node)
        childNodes.remove(node)
    }

    override fun copy(): ITransformNode = G3D.node(nodeType).set(this)

    override fun updateTransform(recursive: Boolean) {
        val parent = parentNode

        worldMatrix.set(position, rotation, scale)
        if (parent != null) worldMatrix.mulLeft(parent.worldMatrix)

        if (recursive) {
            childNodes.traverseSafe {
                it.updateTransform(recursive)
            }
        }
    }

    override fun set(other: ITransformNode): ITransformNode {
        super.set(other)
        position.set(other.position)
        rotation.set(other.rotation)
        scale.set(other.scale)
        worldMatrix.set(other.worldMatrix)
        return this
    }

    override fun reset() {
        position.set(0f, 0f, 0f)
        rotation.idtQuaternion()
        scale.set(1f, 1f, 1f)
        super.reset()
    }
}