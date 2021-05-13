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

package app.thelema.anim

class Frame2DAnimConf(numFramesX: Int = 1, numFramesY: Int = 1) {
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

    var scaleX: Float = 1f / numFramesX
        private set
    var scaleY: Float = 1f / numFramesY
        private set

    var framesNum: Int = numFramesX * numFramesY
        private set
    var startFrame: Int = 0
        private set
    var lastFrame: Int = framesNum - 1
        private set

    var speed: Float = 1f
        set(value) {
            field = value
            frameDelay = 1f / value
        }

    var frameDelay = 1f / speed
        private set

    var loop = true
}