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
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.g3d.scene
import app.thelema.math.*
import kotlin.math.abs

/**
 * @author mzechner, zeganstyl
 */
interface ICamera: IEntityComponent {
    val node: ITransformNode

    val position: IVec3
        get() = node.position

    /** the unit length direction vector of the camera */
    val direction: IVec3
    /** the unit length up vector of the camera */
    val up: IVec3

    /** the projection matrix  */
    val projectionMatrix: IMat4
    /** the view matrix  */
    val viewMatrix: IMat4
    /** the combined projection and view matrix  */
    val viewProjectionMatrix: IMat4

    val previousViewMatrix: IMat4?
    val previousViewProjectMatrix: IMat4?

    /** The inverse combined projection and view matrix. Used in [unproject] */
    val inverseViewProjectionMatrix: IMat4

    val frustum: Frustum

    /** Near clipping plane distance, has to be positive  */
    var near: Float

    /** Far clipping plane distance, has to be positive  */
    var far: Float

    /** Camera width in world units */
    var viewportWidth: Float

    /** Camera height in world units */
    var viewportHeight: Float

    /** Field of view */
    var fov: Float

    var aspectRatio: Float

    var isOrthographic: Boolean

    /** Used for orthographic projection */
    var isCentered: Boolean

    var useViewMatrix: Boolean

    /** Used for orthographic projection */
    var zoom: Float

    var isCameraUpdateRequested: Boolean

    fun makeCameraActive()

    fun enablePreviousMatrix(enable: Boolean = true)

    fun requestCameraUpdate() { isCameraUpdateRequested = true }

    fun setNearFar(near: Float, far: Float)

    fun lookAt(position: IVec3, target: IVec3, up: IVec3 = MATH.Y)

    fun updatePreviousTransform()

    fun updateCamera()

    /** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject, but does not
     * rely on OpenGL. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left corner, y
     * pointing down, x pointing to the right). A z-coordinate of 0 will return a
     * point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to specify the
     * viewport position and dimensions in the coordinate system expected by glViewport(), with the
     * origin in the bottom left corner of the screen.
     * @param screenCoords the point in screen coordinates (origin top left)
     * @return the mutated and unprojected screenCoords [IVec3]
     */
    fun unproject(screenCoords: IVec3): IVec3

    fun unproject(screenCoords: IVec2): IVec2

    /** Projects the [IVec3] given in world space to screen coordinates. It's the same as GLU gluProject with one small
     * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
     * **bottom** left, with the y-axis pointing **upwards** and the x-axis pointing to the right.
     * @return the mutated and projected worldCoords [IVec3]
     */
    fun project(worldPosition: IVec3): IVec3

    fun project(worldPosition: IVec2): IVec2

    /** Creates a picking [Ray] from the coordinates given in screen coordinates. It is assumed that the viewport spans the
     * whole screen. The screen coordinates origin is assumed to be in the top left corner, its y-axis pointing down, the x-axis
     * pointing to the right. The returned instance is not a new instance but an internal member only accessible via this function.
     * @return the picking Ray.
     */
    fun getPickRay(screenX: Float, screenY: Float, out: Ray = Ray()): Ray

    fun reset()
}

/** @author zeganstyl */
class Camera(): ICamera {
    constructor(block: Camera.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateCamera()
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override var node: ITransformNode = TransformNode()

    override val componentName: String
        get() = "Camera"

    override val direction: IVec3 = Vec3(1f, 0f, 0f)
    override val up: IVec3 = Vec3(0f, 1f, 0f)

    override var near: Float = 0.1f
        set(value) {
            field = value
            requestCameraUpdate()
        }
    override var far: Float = 1000f
        set(value) {
            field = value
            requestCameraUpdate()
        }
    override var fov: Float = 60f
        set(value) {
            field = value
            requestCameraUpdate()
        }

    override var aspectRatio: Float = 0f
        get() = if (field <= 0f) viewportWidth / viewportHeight else field

    override var viewportWidth: Float = APP.width.toFloat()
    override var viewportHeight: Float = APP.height.toFloat()

    override var isOrthographic: Boolean = false
    override var zoom: Float = 1f
    override var isCentered: Boolean = true
    override var useViewMatrix: Boolean = true

    override val viewMatrix: IMat4 = Mat4()

    override val projectionMatrix: IMat4 = Mat4().setToProjection(near, far, fov, aspectRatio)
    override val viewProjectionMatrix: IMat4 = Mat4().set(projectionMatrix).mul(viewMatrix)

    private var previousViewProjectionMatrixInternal: IMat4? = if (TransformNode.isPreviousMatrixEnabled) Mat4() else null
    override val previousViewProjectMatrix: IMat4?
        get() = previousViewProjectionMatrixInternal

    private var previousViewMatrixInternal: IMat4? = if (TransformNode.isPreviousMatrixEnabled) Mat4() else null
    override val previousViewMatrix: IMat4?
        get() = previousViewMatrixInternal

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

    override fun makeCameraActive() {
        entity.getRootEntity().scene().activeCamera = this
    }

    override fun unproject(screenCoords: IVec3): IVec3 {
        screenCoords.x = 2f * screenCoords.x / viewportWidth - 1f
        screenCoords.y = 2f * (viewportHeight - screenCoords.y - 1f) / viewportHeight - 1f
        screenCoords.z = 2f * screenCoords.z - 1f
        screenCoords.prj(inverseViewProjectionMatrix)
        return screenCoords
    }

    override fun unproject(screenCoords: IVec2): IVec2 {
        screenCoords.x = 2f * screenCoords.x / viewportWidth - 1f
        screenCoords.y = 2f * (viewportHeight - screenCoords.y - 1f) / viewportHeight - 1f
        screenCoords.prj(inverseViewProjectionMatrix)
        return screenCoords
    }

    override fun project(worldPosition: IVec3): IVec3 {
        worldPosition.prj(viewProjectionMatrix)
        worldPosition.x = viewportWidth * (worldPosition.x + 1f) * 0.5f
        worldPosition.y = viewportHeight * (worldPosition.y + 1f) * 0.5f
        worldPosition.z = (worldPosition.z + 1f) * 0.5f
        return worldPosition
    }

    override fun project(worldPosition: IVec2): IVec2 {
        worldPosition.prj(viewProjectionMatrix)
        worldPosition.x = viewportWidth * (worldPosition.x + 1f) * 0.5f
        worldPosition.y = viewportHeight * (worldPosition.y + 1f) * 0.5f
        return worldPosition
    }

    override fun setNearFar(near: Float, far: Float) {
        this.near = near
        this.far = far
    }

    override fun updatePreviousTransform() {
        previousViewMatrix?.set(viewMatrix)
        previousViewProjectMatrix?.set(viewProjectionMatrix)
    }

    override fun enablePreviousMatrix(enable: Boolean) {
        if (enable) {
            if (previousViewProjectionMatrixInternal == null) {
                previousViewProjectionMatrixInternal = Mat4()
                previousViewMatrixInternal = Mat4()
            }
        } else {
            previousViewProjectionMatrixInternal = null
            previousViewMatrixInternal = null
        }
    }

    override fun lookAt(position: IVec3, target: IVec3, up: IVec3) {
        direction.set(target).sub(position).nor()
        this.position.set(position)
        this.up.set(up)

        requestCameraUpdate()
    }

    override fun reset() {
        node.reset()
        near = 0.1f
        far = 100f
        up.set(0f, 1f, 0f)
        direction.set(1f, 0f, 0f)
        position.set(0f, 0f, 0f)
        node.requestTransformUpdate()

        requestCameraUpdate()
    }

    override fun updateCamera() {
        isCameraUpdateRequested = false

        tmpM.set(viewProjectionMatrix)

        viewMatrix.set(node.worldMatrix)
        viewMatrix.setTransposed3x3From(node.worldMatrix)

        if (isOrthographic) {
            if (isCentered) {
                val widthHalf = zoom * viewportWidth * 0.5f
                val heightHalf = zoom * viewportHeight * 0.5f
                projectionMatrix.setToOrtho(-widthHalf, widthHalf, -heightHalf, heightHalf, near, far)
            } else {
                projectionMatrix.setToOrtho(0f, zoom * viewportWidth, 0f, zoom * viewportHeight, near, far)
            }
        } else {
            projectionMatrix.setToProjection(abs(near), abs(far), fov, aspectRatio)
        }

        viewProjectionMatrix.set(projectionMatrix)
        if (useViewMatrix) {
            viewMatrix.setToLook(position, direction, up)
            viewProjectionMatrix.mul(viewMatrix)
        }

        if (tmpM.isNotEqualTo(viewProjectionMatrix)) {
            inverseViewProjectionNeedUpdate = true
            frustumNeedUpdate = true
        }
    }

    override fun getPickRay(screenX: Float, screenY: Float, out: Ray): Ray {
        unproject(out.origin.set(screenX, screenY, 0f))
        unproject(out.direction.set(screenX, screenY, 1f))
        out.direction.sub(out.origin).nor()
        return out
    }
}

var ActiveCameraVar: ICamera? = null
var ActiveCamera: ICamera
    get() {
        if (ActiveCameraVar == null) {
            ActiveCameraVar = Camera { lookAt(Vec3(3f, 3f, 3f), MATH.Zero3) }
        }
        return ActiveCameraVar!!
    }
    set(value) {
        ActiveCameraVar = value
    }

fun ActiveCamera(block: ICamera.() -> Unit) = block(ActiveCamera)

fun IEntity.camera(block: ICamera.() -> Unit) = component(block)
fun IEntity.camera() = component<ICamera>()
