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

package org.ksdfv.thelema.texture

import org.ksdfv.thelema.math.Mat3

/** give [transform] to texture
 * @author zeganstyl */
class Texture2DAnimation(numFramesX: Int = 1, numFramesY: Int = 1, var transform: Mat3 = Mat3()) {
    var numFramesX: Int = numFramesX
        set(value) {
            field = value
            scaleX = 1f / value
        }

    var numFramesY: Int = numFramesY
        set(value) {
            field = value
            scaleY = 1f / value
        }

    var frame: Int = 0
        set(value) {
            field = value
            translationX = (value % numFramesX) * scaleX
            translationY = (value % numFramesY) * scaleY
        }

    var speed: Float = 1f
        set(value) {
            field = value
            frameDelay = 1f / value
        }

    var loop = true

    private var scaleX: Float = 1f / numFramesX
    private var scaleY: Float = 1f / numFramesY

    private var translationX: Float = 0f
    private var translationY: Float = 0f

    private var frameDelay = 1f / speed

    private var cachedTime = 0f

    fun update(delta: Float) {
        cachedTime += delta

        if (cachedTime >= frameDelay) {
            val prev = frame
            frame += (cachedTime / frameDelay).toInt()
            cachedTime = 0f

            if (!loop && prev > frame) {
                frame = numFramesX * numFramesY - 1
            }
        }

        val floats = transform.values
        floats[Mat3.M02] = translationX
        floats[Mat3.M12] = translationY
        floats[Mat3.M00] = scaleX
        floats[Mat3.M11] = scaleY
    }
}