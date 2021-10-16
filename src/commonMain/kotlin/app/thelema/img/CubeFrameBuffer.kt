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

package app.thelema.img

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.ICamera
import app.thelema.gl.*
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3

/** @author zeganstyl */
class CubeFrameBuffer(
    resolution: Int = 1024,
    pixelFormat: Int = GL_RGBA,
    internalFormat: Int = pixelFormat,
    type: Int = GL_UNSIGNED_BYTE,
    useDepth: Boolean = false
): FrameBufferAdapter() {
    var texture = TextureCube()

    val captureProjection = Mat4().setToProjection(0.1f, 10f, 90f, 1f)
    val cameras: Array<ICamera> = arrayOf(
        createCam(Vec3(1f, 0f, 0f), Vec3(0f, -1f, 0f), resolution),
        createCam(Vec3(-1f, 0f, 0f), Vec3(0f, -1f, 0f), resolution),
        createCam(Vec3(0f, 1f, 0f), Vec3(0f, 0f, 1f), resolution),
        createCam(Vec3(0f, -1f, 0f), Vec3(0f, 0f, -1f), resolution),
        createCam(Vec3(0f, 0f, 1f), Vec3(0f, -1f, 0f), resolution),
        createCam(Vec3(0f, 0f, -1f), Vec3(0f, -1f, 0f), resolution)
    )

    init {
        setResolution(resolution, resolution)

        texture.setupAsRenderTarget(resolution, pixelFormat, internalFormat, type, 0)

        if (useDepth) {
            addAttachment(Attachments.depthRenderBuffer())
        }

        buildAttachments()
    }

    private fun createCam(target: IVec3, up: IVec3, resolution: Int): ICamera = Camera {
        fov = 90f
        viewportWidth = resolution.toFloat()
        viewportHeight = resolution.toFloat()
        lookAt(MATH.Zero3, target, up)
        updateCamera()
    }

    override fun setResolution(width: Int, height: Int) {
        if (width != height) throw IllegalStateException("CubeFrameBuffer: width and height must be equal")
        super.setResolution(width, height)
    }

    inline fun renderCube(width: Int, height: Int, mipLevel: Int, block: (side: Int) -> Unit) {
        GL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBufferHandle)
        GL.glViewport(0, 0, width, height)

        val camera = ActiveCamera

        for (i in 0 until 6) {
            GL.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, texture.textureHandle, mipLevel)
            GL.glClear()
            ActiveCamera = cameras[i]
            block(i)
        }

        ActiveCamera = camera

        GL.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, GL.mainFrameBufferHandle)
        GL.glViewport(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)
    }

    inline fun renderCube(resolution: Int, mipLevel: Int, block: (side: Int) -> Unit) = renderCube(resolution, resolution, mipLevel, block)

    inline fun renderCube(block: (side: Int) -> Unit) = renderCube(width, height, 0, block)
}