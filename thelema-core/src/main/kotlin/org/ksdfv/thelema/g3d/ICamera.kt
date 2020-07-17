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
import org.ksdfv.thelema.g3d.node.TransformNodeType
import org.ksdfv.thelema.math.*
import kotlin.math.abs

/**
 * @author mzechner, zeganstyl
 */
interface ICamera: ITransformNode {
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

    /** World matrix of camera is [viewMatrix] */
    override val worldMatrix: IMat4
        get() = viewMatrix

    override val nodeType: Int
        get() = TransformNodeType.TRS

    override val rotation: IVec4
        get() = IVec4.Zero

    override val scale: IVec3
        get() = IVec3.One

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

    /** Used for orthographic projection */
    var zoom: Float

    fun lookAt(from: IVec3, to: IVec3, up: IVec3 = IVec3.Y) {
        direction.set(to).sub(from).nor()
        position.set(from)
        up.set(up)
        viewMatrix.setToLookAt(from, direction, up)
    }

    fun set(camera: ICamera): ICamera {
        super.set(camera)

        near = camera.near
        far = camera.far

        viewportWidth = camera.viewportWidth
        viewportHeight = camera.viewportHeight

        zoom = camera.zoom
        isOrthographic = camera.isOrthographic

        update()
        return this
    }

    fun updatePreviousTransform() {
        previousViewProjectionMatrix.set(viewProjectionMatrix)
    }

    override fun updateTransform(recursive: Boolean) {
        super.updateTransform(recursive)
        update()
    }

    fun update() {
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
        viewMatrix.setToLookAt(position, direction, up)
        viewProjectionMatrix.mul(viewMatrix)
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

    /** Recalculates the direction of the camera to look at the point (x, y, z). This function assumes the up vector is normalized.
     * @param x the x-coordinate of the point to look at
     * @param y the y-coordinate of the point to look at
     * @param z the z-coordinate of the point to look at
     */
    fun lookAt(x: Float, y: Float, z: Float) {
        tmpVec.set(x, y, z).sub(position).nor()
        if (tmpVec.x != 0f && tmpVec.y != 0f && tmpVec.z != 0f) {
            val dot = tmpVec.dot(up) // up and direction must ALWAYS be orthonormal vectors
            if (abs(dot - 1) < 0.000000001f) {
                // Collinear
                up.set(direction).scl(-1f)
            } else if (abs(dot + 1) < 0.000000001f) {
                // Collinear opposite
                up.set(direction)
            }
            direction.set(tmpVec)
            normalizeUp()
        }
    }

    /** Recalculates the direction of the camera to look at the point (x, y, z).
     * @param target the point to look at
     */
    fun lookAt(target: Vec3) = lookAt(target.x, target.y, target.z)

    /** Normalizes the up vector by first calculating the right vector via a cross product between direction and up, and then
     * recalculating the up vector via a cross product between right and direction.  */
    fun normalizeUp() {
        tmpVec.set(direction).crs(up).nor()
        up.set(tmpVec).crs(direction).nor()
    }

    /** Rotates the direction and up vector of this camera by the given angle around the given axis.
     * The direction and up vector will not be orthogonalized. */
    fun rotate(radians: Float, axisX: Float, axisY: Float, axisZ: Float) {
        direction.rotate(radians, axisX, axisY, axisZ)
        up.rotate(radians, axisX, axisY, axisZ)
    }

    /** Rotates the direction and up vector of this camera by the given angle around the given axis.
     * The direction and up vector will not be orthogonalized. */
    fun rotate(axis: IVec3, radians: Float) {
        direction.rotate(axis, radians)
        up.rotate(axis, radians)
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
        direction.transformByQuaternion(quaternion)
        direction.transformByQuaternion(quaternion)
    }

    /** Transform the position, direction and up vector by the given matrix */
    fun transform(transform: IMat4) {
        position.mul(transform)
        rotate(transform)
    }

    /** Moves the camera by the given amount on each axis. */
    fun translate(x: Float, y: Float, z: Float) = position.add(x, y, z)

    /** Moves the camera by the given vector. */
    fun translate(vec: Vec3) = position.add(vec)

    override fun reset() {
        super.reset()
        near = 0.001f
        far = 1000f
        up.set(IVec3.Y)
        direction.set(IVec3.X)
        position.set(IVec3.Zero)
        update()
    }

    companion object {
        private val tmpVec = Vec3()

        val Default: ICamera = Camera()
            .apply {
            name = "Default"
        }
    }
}