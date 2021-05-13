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

package app.thelema.ui

import app.thelema.app.APP
import app.thelema.g3d.cam.ICamera
import app.thelema.gl.GL
import app.thelema.math.*

/** Manages a [ICamera] and determines how world coordinates are mapped to and from the screen.
 * @author Daniel Holderbaum, Nathan Sweet
 */
abstract class Viewport(var camera: ICamera) {
    /** The virtual width of this viewport in world coordinates. This width is scaled to the viewport's screen width.  */
    var worldWidth: Float = 0f
    /** The virtual height of this viewport in world coordinates. This height is scaled to the viewport's screen height.  */
    var worldHeight: Float = 0f
    /** Sets the viewport's offset from the left edge of the screen. This is typically set by [update].  */
    var screenX: Int = 0
    /** Sets the viewport's offset from the bottom edge of the screen. This is typically set by [update].  */
    var screenY: Int = 0
    /** Sets the viewport's width in screen coordinates. This is typically set by [update].  */
    var screenWidth: Int = 0
    /** Sets the viewport's height in screen coordinates. This is typically set by [update].  */
    var screenHeight: Int = 0

    private val tmp = Vec3()

    private val ray = Ray(Vec3(), Vec3())

    /** Returns the right gutter (black bar) x in screen coordinates.  */
    val rightGutterX: Int
        get() = screenX + screenWidth

    /** Returns the right gutter (black bar) width in screen coordinates.  */
    val rightGutterWidth: Int
        get() = APP.width - (screenX + screenWidth)

    /** Returns the top gutter (black bar) y in screen coordinates.  */
    val topGutterY: Int
        get() = screenY + screenHeight

    /** Returns the top gutter (black bar) height in screen coordinates.  */
    val topGutterHeight: Int
        get() = APP.height - (screenY + screenHeight)

    /** Applies the viewport to the camera and sets the glViewport.
     * @param centerCamera If true, the camera position is set to the center of the world.
     */
    fun apply(centerCamera: Boolean = false) {
        //HdpiUtils.glViewport(screenX, screenY, screenWidth, screenHeight)
        GL.glViewport(screenX, screenY, screenWidth, screenHeight)
        camera.viewportWidth = worldWidth
        camera.viewportHeight = worldHeight
        camera.isCentered = false
        //if (centerCamera) camera.position.set(worldWidth * 0.5f, worldHeight * 0.5f, 0f)
        camera.updateCamera()
    }

    /** Configures this viewport's screen bounds using the specified screen size and calls [apply].
     * The default implementation only calls [apply].  */
    open fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean = false) {
        apply(centerCamera)
    }

    /** Transforms the specified screen coordinate to world coordinates.
     * @return The vector that was passed in, transformed to world coordinates.
     * @see ICamera.unproject
     */
    fun unproject(screenCoords: IVec2): IVec2 {

        tmp.set((screenCoords.x / APP.width) * screenWidth, (screenCoords.y / APP.height) * screenHeight, 0f)
        camera.unproject(tmp, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        screenCoords.set(tmp.x, tmp.y)
        return screenCoords
    }

    /** Transforms the specified world coordinate to screen coordinates.
     * @return The vector that was passed in, transformed to screen coordinates.
     * @see ICamera.project
     */
    fun project(worldCoords: IVec2): IVec2 {
        tmp.set(worldCoords.x, worldCoords.y, 1f)
        camera.project(tmp, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        worldCoords.set(tmp.x, tmp.y)
        return worldCoords
    }

    /** Transforms the specified screen coordinate to world coordinates.
     * @return The vector that was passed in, transformed to world coordinates.
     * @see ICamera.unproject
     */
    fun unproject(screenCoords: IVec3): IVec3 {
        camera.unproject(screenCoords, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        return screenCoords
    }

    /** Transforms the specified world coordinate to screen coordinates.
     * @return The vector that was passed in, transformed to screen coordinates.
     * @see ICamera.project
     */
    fun project(worldCoords: IVec3): IVec3 {
        camera.project(worldCoords, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        return worldCoords
    }

    /** @see ICamera.getPickRay
     */
    fun getPickRay(screenX: Float, screenY: Float): Ray {
        return camera.getPickRay(ray, screenX, screenY, this.screenX.toFloat(), this.screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
    }

    /** @see ScissorStack.calculateScissors
     */
    fun calculateScissors(
        batchTransform: IMat4,
        areaX: Float,
        areaY: Float,
        areaWidth: Float,
        areaHeight: Float,
        scissor: IRectangle
    ) = ScissorStack.calculateScissors(
        camera,
        screenX.toFloat(),
        screenY.toFloat(),
        screenWidth.toFloat(),
        screenHeight.toFloat(),
        batchTransform,
        areaX, areaY, areaWidth, areaHeight, scissor
    )

    /** Transforms a point to real screen coordinates (as opposed to OpenGL ES window coordinates), where the origin is in the top
     * left and the the y-axis is pointing downwards.  */
    fun toScreenCoordinates(worldCoords: IVec2, transformMatrix: IMat4): IVec2 {
        tmp.set(worldCoords.x, worldCoords.y, 0f)
        tmp.mul(transformMatrix)
        camera.project(tmp)
        tmp.y = APP.height - tmp.y
        worldCoords.x = tmp.x
        worldCoords.y = tmp.y
        return worldCoords
    }

    fun setWorldSize(worldWidth: Float, worldHeight: Float) {
        this.worldWidth = worldWidth
        this.worldHeight = worldHeight
    }

    /** Sets the viewport's position in screen coordinates. This is typically set by [update].  */
    fun setScreenPosition(screenX: Int, screenY: Int) {
        this.screenX = screenX
        this.screenY = screenY
    }

    /** Sets the viewport's size in screen coordinates. This is typically set by [update].  */
    fun setScreenSize(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    /** Sets the viewport's bounds in screen coordinates. This is typically set by [update].  */
    fun setScreenBounds(screenX: Int, screenY: Int, screenWidth: Int, screenHeight: Int) {
        this.screenX = screenX
        this.screenY = screenY
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    /** Returns the left gutter (black bar) width in screen coordinates.  */
    fun getLeftGutterWidth(): Int {
        return screenX
    }

    /** Returns the bottom gutter (black bar) height in screen coordinates.  */
    fun getBottomGutterHeight(): Int {
        return screenY
    }
}
