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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3

open class Camera(
    override val position: IVec3 = Vec3(),
    override val direction: IVec3 = Vec3(1f, 0f, 0f),
    override val up: IVec3 = Vec3(0f, 1f, 0f),
    override var near: Float = 0.1f,
    override var far: Float = 100f,
    override var fov: Float = 60f,
    override var viewportWidth: Float = APP.width.toFloat(),
    override var viewportHeight: Float = APP.height.toFloat(),
    override var isOrthographic: Boolean = false,
    override var zoom: Float = 1f,
    override var name: String = ""
): ICamera {
    constructor(from: IVec3, to: IVec3, near: Float = 0.1f, far: Float = 100f): this(near = near, far = far) {
        lookAt(from, to)
        update()
    }

    override val viewMatrix: IMat4 = Mat4()
    override var isCenterInPosition: Boolean = true
    override val projectionMatrix: IMat4 = Mat4().setToProjection(near, far, fov, aspectRatio)
    override val viewProjectionMatrix: IMat4 = Mat4()
    override var previousViewProjectionMatrix: IMat4 = Mat4()
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

    override var parentNode: ITransformNode? = null

    override val childNodes: List<ITransformNode> = ArrayList()

    override fun update() {
        tmpM.set(viewProjectionMatrix)

        super.update()

        if (tmpM.isNotEqualTo(viewProjectionMatrix)) {
            inverseViewProjectionNeedUpdate = true
        }
    }

    override fun copy(): ICamera = Camera().set(this)

    companion object {
        val tmpM = Mat4()
    }
}