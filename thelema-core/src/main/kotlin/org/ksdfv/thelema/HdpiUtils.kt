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

import org.ksdfv.thelema.gl.GL

/** To deal with HDPI monitors properly, use the glViewport and glScissor functions of this class instead of directly calling
 * OpenGL yourself. The logical coordinate system provided by the operating system may not have the same resolution as the actual
 * drawing surface to which OpenGL draws, also known as the backbuffer. This class will ensure, that you pass the correct values
 * to OpenGL for any function that expects backbuffer coordinates instead of logical coordinates.
 *
 * @author badlogic
 */
object HdpiUtils {
    private var mode = HdpiMode.Logical

    /** Calls glScissor, expecting the coordinates and sizes given in logical coordinates and
     * automatically converts them to backbuffer coordinates, which may be bigger on HDPI screens.  */
    fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        if (mode == HdpiMode.Logical && (APP.width != GL.mainFrameBufferWidth
                        || APP.height != GL.mainFrameBufferHeight)) {
            GL.glScissor(toBackBufferX(x), toBackBufferY(y), toBackBufferX(width), toBackBufferY(height))
        } else {
            GL.glScissor(x, y, width, height)
        }
    }

    /**
     * Converts an x-coordinate given in logical screen coordinates to
     * backbuffer coordinates.
     */
    fun toBackBufferX(logicalX: Int): Int {
        return (logicalX * GL.mainFrameBufferWidth / APP.width.toFloat()).toInt()
    }

    /**
     * Convers an y-coordinate given in backbuffer coordinates to
     * logical screen coordinates
     */
    fun toBackBufferY(logicalY: Int): Int {
        return (logicalY * GL.mainFrameBufferHeight / APP.height.toFloat()).toInt()
    }
}
