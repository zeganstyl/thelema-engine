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

package org.ksdfv.thelema.g3d.cam

import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.utils.IPath
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object ActiveCamera: ICamera {
    var proxy: ICamera = Camera()
        set(value) {
            if (value === field) throw IllegalArgumentException("ActiveCamera proxy can't be ActiveCamera")
            field = value
        }

    override var name: String
        get() = proxy.name
        set(value) { proxy.name = value }

    override val position: IVec3
        get() = proxy.position

    override val direction: IVec3
        get() = proxy.direction

    override val up: IVec3
        get() = proxy.up

    override val projectionMatrix: IMat4
        get() = proxy.projectionMatrix

    override val viewMatrix: IMat4
        get() = proxy.viewMatrix

    override val viewProjectionMatrix: IMat4
        get() = proxy.viewProjectionMatrix

    override var previousViewProjectionMatrix: IMat4
        get() = proxy.previousViewProjectionMatrix
        set(value) { proxy.previousViewProjectionMatrix = value }

    override val inverseViewProjectionMatrix: IMat4
        get() = proxy.inverseViewProjectionMatrix

    override val frustum: Frustum
        get() = proxy.frustum

    override var zoom: Float
        get() = proxy.zoom
        set(value) { proxy.zoom = value }

    override var viewportWidth: Float
        get() = proxy.viewportWidth
        set(value) { proxy.viewportWidth = value }

    override var viewportHeight: Float
        get() = proxy.viewportHeight
        set(value) { proxy.viewportHeight = value }

    override val rotation: IVec4
        get() = proxy.rotation

    override val scale: IVec3
        get() = proxy.scale

    override var parentNode: ITransformNode?
        get() = proxy.parentNode
        set(value) { proxy.parentNode = value }

    override val childNodes: List<ITransformNode>
        get() = proxy.childNodes

    override var near: Float
        get() = proxy.near
        set(value) { proxy.near = value }

    override var far: Float
        get() = proxy.far
        set(value) { proxy.far = value }

    override val fov: Float
        get() = proxy.fov

    override var isOrthographic: Boolean
        get() = proxy.isOrthographic
        set(value) { proxy.isOrthographic = value }

    override var isCentered: Boolean
        get() = proxy.isCentered
        set(value) { proxy.isCentered = value }

    override val worldMatrix: IMat4
        get() = proxy.worldMatrix
    override val nodeType: Int
        get() = proxy.nodeType
    override val aspectRatio: Float
        get() = proxy.aspectRatio

    override val parentPath: IPath?
        get() = proxy.parentPath
    override val childPaths: List<IPath>
        get() = proxy.childPaths

    override fun unproject(
        screenCoords: IVec3,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): IVec3 = proxy.unproject(screenCoords, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun project(
        worldCoords: IVec3,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): IVec3 = proxy.project(worldCoords, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun getPickRay(
        out: Ray,
        screenX: Float,
        screenY: Float,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): Ray = proxy.getPickRay(out, screenX, screenY, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun set(camera: ICamera): ICamera = proxy.set(camera)
    override fun reset() = proxy.reset()
    override fun updatePreviousTransform() = proxy.updatePreviousTransform()
    override fun update() = proxy.update()
    override fun copy(): ITransformNode = proxy.copy()
    override fun lookAt(position: IVec3, target: IVec3, up: IVec3) = proxy.lookAt(position, target, up)
    override fun rotate(transform: IMat4) = proxy.rotate(transform)
    override fun rotate(quaternion: IVec4) = proxy.rotate(quaternion)
    override fun transform(transform: IMat4) = proxy.transform(transform)
    override fun translate(x: Float, y: Float, z: Float): IVec3 = proxy.translate(x, y, z)
    override fun translate(vec: Vec3): IVec3 = proxy.translate(vec)

    override fun addChildNode(node: ITransformNode) = proxy.addChildNode(node)
    override fun addChildren(nodes: List<ITransformNode>) = proxy.addChildren(nodes)
    override fun removeChildNode(node: ITransformNode) = proxy.removeChildNode(node)
    override fun detach() = proxy.detach()
    override fun updateTransform(recursive: Boolean) = proxy.updateTransform(recursive)
    override fun traverseChildrenTree(call: (node: ITransformNode) -> Unit) = proxy.traverseChildrenTree(call)
    override fun set(other: ITransformNode): ITransformNode = proxy.set(other)
    override fun clear() = proxy.clear()
}