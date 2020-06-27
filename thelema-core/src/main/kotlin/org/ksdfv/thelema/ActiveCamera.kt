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

package org.ksdfv.thelema

import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.math.*

/** @author zeganstyl */
object ActiveCamera: ICamera {
    var api: ICamera = ICamera.Default

    override var name: String
        get() = api.name
        set(value) { api.name = value }

    override val position: IVec3
        get() = api.position

    override val direction: Vec3
        get() = api.direction

    override val up: Vec3
        get() = api.up

    override val projectionMatrix: IMat4
        get() = api.projectionMatrix

    override val viewMatrix: IMat4
        get() = api.viewMatrix

    override val viewProjectionMatrix: IMat4
        get() = api.viewProjectionMatrix

    override val inverseViewProjectionMatrix: IMat4
        get() = api.inverseViewProjectionMatrix

    override var zoom: Float
        get() = api.zoom
        set(value) { api.zoom = value }

    override var viewportWidth: Float
        get() = api.viewportWidth
        set(value) { api.viewportWidth = value }

    override var viewportHeight: Float
        get() = api.viewportHeight
        set(value) { api.viewportHeight = value }

    override val rotation: IVec4
        get() = api.rotation

    override val scale: IVec3
        get() = api.scale

    override var parentNode: ITransformNode?
        get() = api.parentNode
        set(value) { api.parentNode = value }

    override val childNodes: List<ITransformNode>
        get() = api.childNodes

    override var near: Float
        get() = api.near
        set(value) { api.near = value }

    override var far: Float
        get() = api.far
        set(value) { api.far = value }

    override val fov: Float
        get() = api.fov

    override var isOrthographic: Boolean
        get() = api.isOrthographic
        set(value) { api.isOrthographic = value }

    override var isCenterInPosition: Boolean
        get() = api.isCenterInPosition
        set(value) { api.isCenterInPosition = value }

    override val worldMatrix: IMat4
        get() = api.worldMatrix

    override val nodeType: Int
        get() = api.nodeType

    override val aspectRatio: Float
        get() = api.aspectRatio

    override fun lookAt(from: IVec3, to: IVec3, up: IVec3) = api.lookAt(from, to, up)

    override fun set(camera: ICamera): ICamera = api.set(camera)

    override fun unproject(
        screenCoords: IVec3,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): IVec3 = api.unproject(screenCoords, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun project(
        worldCoords: IVec3,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): IVec3 = api.project(worldCoords, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun getPickRay(
        out: Ray,
        screenX: Float,
        screenY: Float,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): Ray = api.getPickRay(out, screenX, screenY, viewportX, viewportY, viewportWidth, viewportHeight)

    override fun reset() = api.reset()

    override fun update() = api.update()

    override fun copy(): ITransformNode = api.copy()
}