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
import org.ksdfv.thelema.math.*

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

    override fun copy(): ITransformNode = ITransformNode.Build().set(this)

    override fun updateTransform(recursive: Boolean) {
        val parent = parentNode
        if (parent != null) {
            worldMatrix.set(position, rotation, scale).mulLeft(parent.getWorldMatrix())
        } else {
            worldMatrix.set(position, rotation, scale)
        }

        if (recursive) {
            childNodes.traverseSafe {
                it.updateTransform(recursive)
            }
        }
    }

    override fun applyTo(vec: IVec3) {
        vec.mul(worldMatrix)
    }

    override fun applyTo(mat: IMat4) {
        mat.mul(worldMatrix)
    }

    override fun getWorldMatrix(out: IMat4, isLocal: Boolean): IMat4 {
        if (isLocal) {
            out.set(position, rotation, scale)
        } else {
            out.set(worldMatrix)
        }
        return out
    }

    override fun setPosition(vec: IVec3): ITransformNode {
        super.setPosition(vec)
        return this
    }

    override fun setWorldMatrix(mat: IMat4): ITransformNode {
        mat.getTranslation(position)
        mat.getRotation(rotation)
        mat.getScale(scale)
        return this
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