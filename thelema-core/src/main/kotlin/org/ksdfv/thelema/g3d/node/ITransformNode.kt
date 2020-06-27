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

import org.ksdfv.thelema.IPath
import org.ksdfv.thelema.ext.traverseSafe
import org.ksdfv.thelema.math.*

/** Spatial transform node. Node may contain children and form a hierarchy tree of nodes.
 * Node may contain any transform data, depended on [nodeType].
 * Nodes with less data, may be more optimal in calculations and memory consumption.
 * @author zeganstyl */
interface ITransformNode: IPath {
    /** Local translation, relative to the parent. */
    val position: IVec3

    /** Local rotation, relative to the parent. It may not work, check type of this node [TransformNodeType]. */
    val rotation: IVec4

    /** Local scale, relative to the parent. It may not work, check type of this node [TransformNodeType]. */
    val scale: IVec3

    /** Global world transform, product of multiply local and parent transform data, calculated via [updateTransform]. */
    val worldMatrix: IMat4

    var parentNode: ITransformNode?

    override val parentPath: IPath?
        get() = parentNode

    val childNodes: List<ITransformNode>

    override val childPaths: List<IPath>
        get() = childNodes

    /** See [TransformNodeType] */
    val nodeType: Int
        get() = TransformNodeType.TRS

    fun addChildNode(node: ITransformNode) {
        node.parentNode = this
    }

    fun addChildren(nodes: List<ITransformNode>) {
        for (i in nodes.indices) {
            addChildNode(nodes[i])
        }
    }

    fun removeChildNode(node: ITransformNode) {
        node.parentNode = null
    }

    fun detach() {
        parentNode?.removeChildNode(this)
    }

    /** Calculates the local and world transform of this node and optionally all its children. */
    fun updateTransform(recursive: Boolean = true) {}

    fun traverseChildrenTree(call: (node: ITransformNode) -> Unit) {
        childNodes.traverseSafe {
            call(it)
            it.traverseChildrenTree(call)
        }
    }

    fun reset() {
        updateTransform()
    }

    fun set(other: ITransformNode): ITransformNode {
        name = other.name
        return this
    }

    fun copy(): ITransformNode

    fun getPosition(out: IVec3 = tmp, isLocal: Boolean = false): IVec3 = out.set(position)
    
    fun getWorldMatrix(out: IMat4 = tmpM, isLocal: Boolean = false): IMat4 = out

    /** Squared distance to position. By default in global world space */
    fun dst2(node: ITransformNode, isLocal: Boolean = false): Float = getPosition(
        tmp, isLocal).dst2(node.getPosition(tmp2, isLocal))

    /** Squared distance to position. By default in global world space */
    fun dst2(position: IVec3, isLocal: Boolean = false): Float = getPosition(tmp, isLocal).dst2(position)

    /** Set local position */
    fun setPosition(vec: IVec3): ITransformNode {
        position.set(vec)
        return this
    }

    /** Set local world matrix */
    fun setWorldMatrix(mat: IMat4): ITransformNode = this

    /** Transform [vec] with global transformation data of this node */
    fun applyTo(vec: IVec3) {}

    /** Transform [mat] with global spatial data of this node */
    fun applyTo(mat: IMat4) {}

    /** Clear data, unlink children and unlink from parent */
    fun clear() {
        parentNode?.removeChildNode(this)

        childNodes.traverseSafe { removeChildNode(it) }
    }

    companion object {
        /** Default implementation for this interface */
        var Build: () -> ITransformNode = { Node() }

        val Default = Node().apply { name = "Default" }

        private val tmp = Vec3()
        private val tmp2 = Vec3()

        private val tmpM = Mat4()
    }
}