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
import app.thelema.ecs.ECS
import app.thelema.math.MATH
import kotlin.math.abs

/** @author Xoppa, zeganstyl */
class AnimationAction: ActionAdapter() {
    override val componentName: String
        get() = "AnimationAction"

    var player: AnimationPlayer? = null

    /** The animation to be applied.  */
    var animation: IAnimation? = null
    /** The speed at which to play the animation (can be negative), 1.0 for normal speed.  */
    var speed = 0f
    /** The current animation time.  */
    var time = 0f
    /** The offset within the animation (animation time = offsetTime + time)  */
    var offset = 0f
    /** The duration of the animation  */
    var duration = 0f
        get() {
            return if (field < 0f) {
                val anim = animation
                if (anim != null) anim.duration - offset else 0f
            } else field
        }
    /** The number of remaining loops, negative for continuous, zero if stopped.  */
    var loopCount = 0

    var previousTime: Float = 0f

    override var actionData: ActionData = ActionData(null, 0f, 0f, 0f, 0f, 0, 0f)

    fun end() {
        loopCount = 0
    }

    override fun update(delta: Float): Float {
        previousTime = time

        if (loopCount != 0 && animation != null) {
            var loops: Int
            val diff = speed * delta

            val duration = duration
            if (MATH.isNotZero(duration)) {
                time += diff
                loops = abs(time / duration).toInt()
                if (time < 0f) {
                    loops++
                    while (time < 0f) {
                        time += duration
                    }
                }
                time = abs(time % duration)
            } else {
                loops = 1
            }

            for (i in 0 until loops) {
                if (loopCount > 0) loopCount--
                if (loopCount == 0) {
                    val result = (loops - 1 - i) * duration + if (diff < 0f) duration - time else time
                    time = if (diff < 0f) 0f else duration
                    return result
                }
            }

            return 0f
        } else
            return delta
    }
}