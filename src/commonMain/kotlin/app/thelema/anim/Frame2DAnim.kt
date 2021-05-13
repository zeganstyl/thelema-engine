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

import app.thelema.action.ActionAdapter
import app.thelema.action.ActionData
import app.thelema.action.IAction
import app.thelema.ecs.IEntity
import app.thelema.math.Mat3

/** For sprite sheet animation
 * @author zeganstyl */
class Frame2DAnim(var conf: Frame2DAnimConf): ActionAdapter() {
    override val componentName: String
        get() = "Frame2DAnim"

    var frame: Int = 0
        set(value) {
            if (field != value) {
                field = value % conf.framesNum
                translationX = (value % conf.numFramesX) * conf.scaleX
                translationY = ((value / conf.numFramesY) % conf.numFramesY) * conf.scaleY
            }
        }

    var translationX: Float = 0f
        private set
    var translationY: Float = 0f
        private set

    private var cachedTime = 0f

    override var actionData: ActionData = ActionData(0, 0f, 0f, 0f)

    override fun restart() {
        frame = conf.startFrame
    }

    override fun update(delta: Float): Float {
        isRunning = frame != conf.lastFrame || conf.loop
        if (isRunning) {
            cachedTime += delta

            if (cachedTime >= conf.frameDelay) {
                val prev = frame
                frame += (cachedTime / conf.frameDelay).toInt()
                cachedTime = 0f

                if (!conf.loop && prev > frame) {
                    frame = conf.lastFrame
                }
            }
        }
        return 0f
    }

    /** If shader uses matrix to transform texture UVs,
     * you can use this function to setup matrix for current frame. */
    fun setTo(mat: Mat3) {
        mat.m02 = translationX
        mat.m12 = translationY
        mat.m00 = conf.scaleX
        mat.m11 = conf.scaleY
    }
}