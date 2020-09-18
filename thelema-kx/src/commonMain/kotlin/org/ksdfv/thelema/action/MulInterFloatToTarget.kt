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

import kotlin.math.abs

/** Every iteration value will be multiplied with mulStep.
 * @author zeganstyl */
class MulInterFloatToTarget(
    var mulStep: Float,
    var target: Float,
    var margin: Float = 0.1f,
    var getValue: () -> Float,
    var setValue: (newValue: Float) -> Unit
): IAction {
    override var isRunning: Boolean = true

    override fun reset() {
        isRunning = true
    }

    override fun update(delta: Float) {
        if (isRunning) {
            val mul = mulStep * delta
            val current = getValue()
            val diff = target - current
            if (abs(diff) > margin) {
                setValue(current + diff * mul)
            } else {
                setValue(target)
                isRunning = false
            }
        }
    }
}