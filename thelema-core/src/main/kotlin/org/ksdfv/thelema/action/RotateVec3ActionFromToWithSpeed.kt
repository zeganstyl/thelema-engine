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

package org.ksdfv.thelema.action

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3

/** Lerp some variable.
 * For variable must defined getter [getValue] and setter [setValue].
 * [speed] in seconds.
 * @author zeganstyl */
class RotateVec3ActionFromToWithSpeed(
    var speed: Float,
    var axis: IVec3,
    var from: IVec3,
    var to: IVec3,
    var variable: IVec3
): IAction {
    override var isRunning: Boolean = true

    val tmp = Vec3()

    override fun reset() {
        isRunning = true
    }

    override fun update(delta: Float) {
        if (!isRunning) {
//            val variable = variable
//            val speed = speed * delta
//            if (target.dst2(variable) > speed * speed) {
//                tmp.set(target).sub(variable).nor().scl(speed)
//                variable.add(tmp)
//            } else {
//                variable.set(target)
//                isRunning = false
//            }
        }
    }
}