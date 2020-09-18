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

/** [updateCall] must return true if action must be ended.
 * @author zeganstyl */
class LambdaDelegateAction(var updateCall: (delta: Float) -> Boolean): IAction {
    override var isRunning: Boolean = true

    override fun reset() {
        isRunning = false
    }

    override fun update(delta: Float) {
        if (isRunning) {
            isRunning = updateCall(delta)
        }
    }
}