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
import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.node.ITransformNode
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

    var previousViewProjectionMatrix: IMat4

    /** The inverse combined projection and view matrix. Used in [unproject] */
    val inverseViewProjectionMatrix: IMat4

    val frustum: Frustum

    /** Near clipping plane distance, has to be positive  */
    var near: Float

    /** Far clipping plane distance, has to be positive  */
    var far: Float

    var viewportWidth: Float
    var viewportHeight: Float

    /** Field of view */
    val fov: Float

    val aspectRatio
        get() = viewportWidth / viewportHeight

    var isOrthographic: Boolean

    /** Used for orthographic projection */
    var isCentered: Boolean

    var useViewMatrix: Boolean

    /** Used for orthographic projection */
    var zoom: Float

    var isCameraUpdateRequested: Boolean

    fun requestCameraUpdate() { isCameraUpdateRequested = true }

    fun lookAt(position: IVec3, target: IVec3, up: IVec3 = MATH.Y) {
        direction.set(target).sub(position).nor()
        this.position.set(position)
        this.up.set(up)
    }

    fun set(camera: ICamera): ICamera {
        super.setComponent(camera)

        near = camera.near
        far = camera.far

        viewportWidth = camera.viewportWidth
        viewportHeight = camera.viewportHeight

        zoom = camera.zoom
        isOrthographic = camera.isOrthographic

        updateCamera()
        return this
    }

    fun setupFor2D() {
        near = 0f
        far = 1f
        isOrthographic = true
        isCentered = true
        direction.set(0f, 0f, 1f)
        up.set(0f, 1f, 0f)
        projectionMatrix.setToOrtho(0f, viewportWidth, 0f, viewportHeight, near, far)
    }

    fun setupForUI() {
        near = 0f
        far = 1f
        isOrthographic = true
        isCentered = true
        direction.set(1f, 0f, 0f)
        up.set(0f, 1f, 0f)
        projectionMatrix.setToOrtho(0f, viewportWidth, 0f, viewportHeight, near, far)
    }

    fun updatePreviousTransform() {
        previousViewProjectionMatrix.set(viewProjectionMatrix)
    }

    fun updateCamera() {
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
    }

    /** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject, but does not
     * rely on OpenGL. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left corner, y
     * pointing down, x pointing to the right). A z-coordinate of 0 will return a
     * point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to specify the
     * viewport position and dimensions in the coordinate system expected by glViewport(), with the
     * origin in the bottom left corner of the screen.
     * @param screenCoords the point in screen coordinates (origin top left)
     * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportWidth the width of the viewport in pixels
     * @param viewportHeight the height of the viewport in pixels
     * @return the mutated and unprojected screenCoords [IVec3]
     */
    fun unproject(
        screenCoords: IVec3,
        viewportX: Float = 0f,
        viewportY: Float = 0f,
        viewportWidth: Float = this.viewportWidth,
        viewportHeight: Float = this.viewportHeight
    ): IVec3 {
        var x = screenCoords.x
        var y = screenCoords.y
        x -= viewportX
        y = viewportHeight - y - 1f
        y -= viewportY
        screenCoords.x = 2 * x / viewportWidth - 1
        screenCoords.y = 2 * y / viewportHeight - 1
        screenCoords.z = 2 * screenCoords.z - 1
        screenCoords.prj(inverseViewProjectionMatrix)
        return screenCoords
    }

    /** Projects the [IVec3] given in world space to screen coordinates. It's the same as GLU gluProject with one small
     * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
     * **bottom** left, with the y-axis pointing **upwards** and the x-axis pointing to the right.
     * This method allows you to specify the viewport position and
     * dimensions in the coordinate system expected by glViewport(), with the origin in the bottom
     * left corner of the screen.
     * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportWidth the width of the viewport in pixels
     * @param viewportHeight the height of the viewport in pixels
     * @return the mutated and projected worldCoords [IVec3]
     */
    fun project(
        worldCoords: IVec3,
        viewportX: Float = 0f,
        viewportY: Float = 0f,
        viewportWidth: Float = this.viewportWidth,
        viewportHeight: Float = this.viewportHeight
    ): IVec3 {
        worldCoords.prj(viewProjectionMatrix)
        worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2 + viewportX
        worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2 + viewportY
        worldCoords.z = (worldCoords.z + 1) / 2
        return worldCoords
    }

    /** Creates a picking [Ray] from the coordinates given in screen coordinates. It is assumed that the viewport spans the
     * whole screen. The screen coordinates origin is assumed to be in the top left corner, its y-axis pointing down, the x-axis
     * pointing to the right. The returned instance is not a new instance but an internal member only accessible via this function.
     * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportWidth the width of the viewport in pixels
     * @param viewportHeight the height of the viewport in pixels
     * @return the picking Ray.
     */
    fun getPickRay(
        out: Ray,
        screenX: Float,
        screenY: Float,
        viewportX: Float = 0f,
        viewportY: Float = 0f,
        viewportWidth: Float = APP.width.toFloat(),
        viewportHeight: Float = APP.height.toFloat()): Ray {
        unproject(out.origin.set(screenX, screenY, 0f), viewportX, viewportY, viewportWidth, viewportHeight)
        unproject(out.direction.set(screenX, screenY, 1f), viewportX, viewportY, viewportWidth, viewportHeight)
        out.direction.sub(out.origin).nor()
        return out
    }

    /** Rotates the direction and up vector of this camera by the given rotation matrix.
     * The direction and up vector will not be orthogonalized. */
    fun rotate(transform: IMat4) {
        direction.rot(transform)
        up.rot(transform)
    }

    /** Rotates the direction and up vector of this camera by the given [IVec4].
     * The direction and up vector will not be orthogonalized. */
    fun rotate(quaternion: IVec4) {
        quaternion.rotateVec3(direction)
        quaternion.rotateVec3(up)
    }

    /** Transform the position, direction and up vector by the given matrix */
    fun transform(transform: IMat4) {
        position.mul(transform)
        rotate(transform)
    }

    fun reset() {
        node.reset()
        near = 0.001f
        far = 1000f
        up.set(0f, 1f, 0f)
        direction.set(1f, 0f, 0f)
        position.set(0f, 0f, 0f)
        node.updateTransform()
    }
}