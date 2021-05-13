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

package app.thelema.action

/** @author zeganstyl */
class DelayAction: ActionAdapter() {
    override val componentName: String
        get() = "DelayAction"

    var delay: Float
        get() = actionData.getTyped(Delay)
        set(value) {
            actionData[Delay] = value
        }

    var remain: Float
        get() = actionData.getTyped(Remain)
        set(value) {
            actionData[Remain] = value
        }

    override var isRunning: Boolean
        get() = remain > 0f
        set(_) {}

    override var actionData: ActionData = ActionData(0f, 0f)

    override fun restart() {
        remain = delay
    }

    override fun update(delta: Float): Float {
        if (remain > 0f) {
            remain -= delta
        }
        return 0f
    }

    companion object {
        private const val Delay = 0
        private const val Remain = 1
    }
}