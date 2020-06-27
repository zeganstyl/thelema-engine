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

/** A viewport where the world size is based on the size of the screen. By default 1 world unit == 1 screen pixel, but this ratio
 * can be [changed][.setUnitsPerPixel].
 * @author Daniel Holderbaum, Nathan Sweet
 */
class ScreenViewport(camera: ICamera = ICamera.Default) : Viewport(camera) {
    /** Sets the number of pixels for each world unit. Eg, a scale of 2.5 means there are 2.5 world units for every 1 screen pixel.
     * Default is 1.  */
    var unitsPerPixel = 1f

    override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
        setScreenBounds(0, 0, screenWidth, screenHeight)
        setWorldSize(screenWidth * unitsPerPixel, screenHeight * unitsPerPixel)
        apply(centerCamera)
    }
}
