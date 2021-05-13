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

import app.thelema.ecs.PropertyType
import app.thelema.ecs.sibling

class BiOperation: ActionAdapter() {
    override val componentName: String
        get() = "BiOperation"

    override var actionData: ActionData = ActionData()

    var first: IAction? = null
        set(value) {
            field = value
            sibling<IAction>().returnValue.type = value?.returnValue?.type ?: PropertyType.Unknown
        }

    var operationType: String = ""

    var second: IAction? = null

    inline fun <reified T: IAction> setFirst(block: T.() -> Unit) {
        first = action(block = block)
    }
    inline fun <reified T: IAction> setSecond(block: T.() -> Unit) {
        second = action(block = block)
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            val first = first
            val second = second
            if (first != null && second != null) {
                if (first.isRunning) first.update(delta)
                if (second.isRunning) second.update(delta)
                if (!(first.isRunning || second.isRunning)) {
                    when (operationType) {
                        OperationType.Plus -> { sibling<IAction>().returnValue.value = first.returnValue.plus(second.returnValue) }
                        OperationType.Equal -> { sibling<IAction>().returnValue.value = first.returnValue.isEqual(second.returnValue) }
                    }
                    isRunning = false
                }
            }
        }
        return 0f
    }
}