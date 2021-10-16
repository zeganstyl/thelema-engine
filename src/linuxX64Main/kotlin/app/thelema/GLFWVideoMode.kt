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

package app.thelema

import glfw.GLFWvidmode
import kotlinx.cinterop.CPointer

class GLFWVideoMode (
    val monitor: CPointer<cnames.structs.GLFWmonitor>,
    val videoMode: GLFWvidmode
) {
    /** the width in physical pixels  */
    val width: Int
        get() = videoMode.width
    /** the height in physical pixles  */
    val height: Int
        get() = videoMode.height
    /** the refresh rate in Hertz  */
    val refreshRate: Int
        get() = videoMode.refreshRate
    /** the number of bits per pixel, may exclude alpha  */
    val bitsPerPixel: Int = videoMode.redBits + videoMode.greenBits + videoMode.blueBits

    override fun toString(): String =
        "$width x $height, bpp: $bitsPerPixel, hz: $refreshRate"
}