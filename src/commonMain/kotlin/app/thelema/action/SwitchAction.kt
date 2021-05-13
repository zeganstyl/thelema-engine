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

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.ecs.getComponentOrNull

class SwitchAction: ActionAdapter() {
    override val componentName: String
        get() = "SwitchAction"

    override var actionData: ActionData = ActionData()

    private var currentCondition: IAction? = null
    private var currentAction: IAction? = null
    private var currentIndex: Int = 0

    override fun restart() {
        super.restart()
        currentCondition = null
        currentAction = null
        currentIndex = 0
        entity.children.forEach { it.getComponentOrNull<IAction>()?.restart() }
    }

    inline fun <reified C: IAction, reified B: IAction> addBranch(condition: C.() -> Unit, body: B.() -> Unit) {
        action(block = condition)
        action(block = body)
    }

    inline fun <reified T: IAction> elseBranch(body: T.() -> Unit) {
        getOrCreateEntity().addEntityWithCorrectedName(Entity(T::class.simpleName!!).apply { component<T>().apply(body) })
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            val children = entity.children
            val action = children[currentIndex].getComponentOrNull<IAction>()
            if (action != null) {
                if (action.isRunning) {
                    action.update(delta)
                } else {
                    // action is completed
                    if (children.size % 2 == 1 && currentIndex == children.size - 1) {
                        // action is last else-body
                        isRunning = false
                    } else {
                        if (currentIndex % 2 == 0) {
                            // action is condition
                            if (action.returnValue.value == true) {
                                currentIndex++
                            } else {
                                currentIndex += 2
                            }
                        } else {
                            // action is body
                            isRunning = false
                        }
                    }
                }
            }
        }

        return 0f
    }
}