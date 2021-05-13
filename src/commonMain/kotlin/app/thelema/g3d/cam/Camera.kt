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

package app.thelema.g3d.cam

import app.thelema.app.APP
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.node.ITransformNode
import app.thelema.g3d.node.TransformNode
import app.thelema.math.*

/** @author zeganstyl */
class Camera(): ICamera {
    constructor(block: Camera.() -> Unit): this() {
        block(this)
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override var node: ITransformNode = TransformNode()

    override val componentName: String
        get() = Name

    override val direction: IVec3 = Vec3(1f, 0f, 0f)
    override val up: IVec3 = Vec3(0f, 1f, 0f)
    override var near: Float = 0.1f
        set(value) {
            field = value
            requestCameraUpdate()
        }
    override var far: Float = 100f
        set(value) {
            field = value
            requestCameraUpdate()
        }
    override var fov: Float = 60f
        set(value) {
            field = value
            requestCameraUpdate()
        }
    override var viewportWidth: Float = APP.width.toFloat()
    override var viewportHeight: Float = APP.height.toFloat()
    override var isOrthographic: Boolean = false
    override var zoom: Float = 1f
    override var isCentered: Boolean = true
    override var useViewMatrix: Boolean = true

    override val viewMatrix: IMat4 = Mat4()

    override val projectionMatrix: IMat4 = Mat4().setToProjection(near, far, fov, aspectRatio)
    override val viewProjectionMatrix: IMat4 = Mat4().set(projectionMatrix).mul(projectionMatrix)

    override var previousViewProjectionMatrix: IMat4 = Mat4()

    override var isCameraUpdateRequested: Boolean = true

    private var inverseViewProjectionInternal: IMat4? = null
    private var inverseViewProjectionNeedUpdate = false

    /** Lazy creating and update on demand */
    override val inverseViewProjectionMatrix: IMat4
        get() {
            if (inverseViewProjectionInternal == null) {
                inverseViewProjectionInternal = Mat4()
                inverseViewProjectionNeedUpdate = true
            }

            if (inverseViewProjectionNeedUpdate) {
                inverseViewProjectionInternal!!.set(viewProjectionMatrix).inv()
                inverseViewProjectionNeedUpdate = false
            }

            return inverseViewProjectionInternal!!
        }

    private var frustumInternal: Frustum? = null
    private var frustumNeedUpdate = false

    /** Lazy creating and update on demand */
    override val frustum: Frustum
        get() {
            if (frustumInternal == null) {
                frustumInternal = Frustum(inverseViewProjectionMatrix)
                frustumNeedUpdate = true
            }

            if (frustumNeedUpdate) {
                frustumInternal!!.setFromMatrix(inverseViewProjectionMatrix)
                frustumNeedUpdate = false
            }

            return frustumInternal!!
        }

    protected val tmpM = Mat4()

    init {
        viewMatrix.setToLook(position, direction, up)
    }

    override fun lookAt(position: IVec3, target: IVec3, up: IVec3) {
        super.lookAt(position, target, up)
        requestCameraUpdate()
    }

    override fun reset() {
        super.reset()
        requestCameraUpdate()
    }

    override fun transform(transform: IMat4) {
        super.transform(transform)
        requestCameraUpdate()
    }

    override fun rotate(quaternion: IVec4) {
        super.rotate(quaternion)
        requestCameraUpdate()
    }

    override fun rotate(transform: IMat4) {
        super.rotate(transform)
        requestCameraUpdate()
    }

    override fun updateCamera() {
        isCameraUpdateRequested = false

        tmpM.set(viewProjectionMatrix)

        viewMatrix.set(node.worldMatrix)
        viewMatrix.setTransposed3x3From(node.worldMatrix)

        super.updateCamera()

        if (tmpM.isNotEqualTo(viewProjectionMatrix)) {
            inverseViewProjectionNeedUpdate = true
            frustumNeedUpdate = true
        }
    }

    companion object {
        const val Name = "Camera"
    }
}