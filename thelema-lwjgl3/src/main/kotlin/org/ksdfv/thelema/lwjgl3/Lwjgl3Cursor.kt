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

package org.ksdfv.thelema.lwjgl3

import org.ksdfv.thelema.ICursor
import org.ksdfv.thelema.img.Pixmap
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import java.nio.ByteBuffer
import java.util.*


class Lwjgl3Cursor internal constructor(val window: Lwjgl3Window, pixmap: Pixmap, xHotspot: Int, yHotspot: Int) : ICursor {
    var pixmapCopy: Pixmap?
    var glfwImage: GLFWImage
    val glfwCursor: Long
    override fun dispose() {
        if (pixmapCopy == null) {
            throw IllegalStateException("Cursor already disposed")
        }
        cursors.remove(this)
        pixmapCopy!!.dispose()
        pixmapCopy = null
        glfwImage.free()
        GLFW.glfwDestroyCursor(glfwCursor)
    }

    companion object {
        val cursors = ArrayList<Lwjgl3Cursor>()
        val systemCursors: MutableMap<ICursor.SystemCursor, Long> = EnumMap(ICursor.SystemCursor::class.java)
        fun dispose(window: Lwjgl3Window) {
            for (i in cursors.size - 1 downTo 0) {
                val cursor = cursors[i]
                if (cursor.window == window) {
                    cursors.removeAt(i).dispose()
                }
            }
        }

        fun disposeSystemCursors() {
            for (systemCursor in systemCursors.values) {
                GLFW.glfwDestroyCursor(systemCursor)
            }
            systemCursors.clear()
        }

        fun setSystemCursor(windowHandle: Long, systemCursor: ICursor.SystemCursor) {
            var glfwCursor = systemCursors[systemCursor]
            if (glfwCursor == null) {
                var handle: Long = 0
                handle = when (systemCursor) {
                    ICursor.SystemCursor.Arrow -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
                    }
                    ICursor.SystemCursor.Crosshair -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR)
                    }
                    ICursor.SystemCursor.Hand -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
                    }
                    ICursor.SystemCursor.HorizontalResize -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR)
                    }
                    ICursor.SystemCursor.VerticalResize -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR)
                    }
                    ICursor.SystemCursor.Ibeam -> {
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR)
                    }
                }
                if (handle == 0L) {
                    return
                }
                glfwCursor = handle
                systemCursors[systemCursor] = glfwCursor
            }
            GLFW.glfwSetCursor(windowHandle, glfwCursor)
        }
    }

    init {
        if (pixmap.format != Pixmap.RGBA8888Format) {
            throw IllegalArgumentException("Cursor image pixmap is not in RGBA8888 format.")
        }
        if (pixmap.width and pixmap.width - 1 != 0) {
            throw IllegalArgumentException(
                    "Cursor image pixmap width of " + pixmap.width + " is not a power-of-two greater than zero.")
        }
        if (pixmap.height and pixmap.height - 1 != 0) {
            throw IllegalArgumentException("Cursor image pixmap height of " + pixmap.height
                    + " is not a power-of-two greater than zero.")
        }
        if (xHotspot < 0 || xHotspot >= pixmap.width) {
            throw IllegalArgumentException("xHotspot coordinate of " + xHotspot
                    + " is not within image width bounds: [0, " + pixmap.width + ").")
        }
        if (yHotspot < 0 || yHotspot >= pixmap.height) {
            throw IllegalArgumentException("yHotspot coordinate of " + yHotspot
                    + " is not within image height bounds: [0, " + pixmap.height + ").")
        }
        pixmapCopy = Pixmap(pixmap.width, pixmap.height, Pixmap.RGBA8888Format)
        pixmapCopy!!.blending = Pixmap.NoneBlend
        pixmapCopy!!.drawPixmap(pixmap, 0, 0)
        glfwImage = GLFWImage.malloc()
        glfwImage.width(pixmapCopy!!.width)
        glfwImage.height(pixmapCopy!!.height)
        glfwImage.pixels(pixmapCopy!!.pixels.sourceObject as ByteBuffer)
        glfwCursor = GLFW.glfwCreateCursor(glfwImage, xHotspot, yHotspot)
        cursors.add(this)
    }
}