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

import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4

/** Node that delegates methods and properties to [node]
 *
 * @author zeganstyl */
interface IDelegateTransformNode: ITransformNode {
    /** Delegate node */
    var node: ITransformNode

    override val position: IVec3
        get() = node.position
    override val rotation: IVec4
        get() = node.rotation
    override val scale: IVec3
        get() = node.scale
    override val worldMatrix: IMat4
        get() = node.worldMatrix
    override var parentNode: ITransformNode?
        get() = node.parentNode
        set(value) { node.parentNode = value }
    override val childNodes: List<ITransformNode>
        get() = node.childNodes
    override val nodeType: Int
        get() = node.nodeType

    override fun updateTransform(recursive: Boolean) = node.updateTransform(recursive)
    override fun addChildNode(node: ITransformNode) = node.addChildNode(node)
    override fun addChildren(nodes: List<ITransformNode>) = node.addChildren(nodes)
    override fun removeChildNode(node: ITransformNode) = node.removeChildNode(node)
    override fun detach() = node.detach()
    override fun traverseChildrenTree(call: (node: ITransformNode) -> Unit) = node.traverseChildrenTree(call)
    override fun reset() = node.reset()
    override fun getPosition(out: IVec3, isLocal: Boolean): IVec3 = node.getPosition(out, isLocal)
    override fun getWorldMatrix(out: IMat4, isLocal: Boolean): IMat4 = node.getWorldMatrix(out, isLocal)
    override fun dst2(node: ITransformNode, isLocal: Boolean): Float = node.dst2(node, isLocal)
    override fun dst2(position: IVec3, isLocal: Boolean): Float  = node.dst2(position, isLocal)
    override fun setPosition(vec: IVec3): ITransformNode = node.setPosition(vec)
    override fun setWorldMatrix(mat: IMat4): ITransformNode = node.setWorldMatrix(mat)
    override fun applyTo(vec: IVec3) = node.applyTo(vec)
    override fun applyTo(mat: IMat4) = node.applyTo(mat)
    override fun clear() = node.clear()
    override fun copy(): ITransformNode = node.copy()
    override fun set(other: ITransformNode): ITransformNode = node.set(other)
}