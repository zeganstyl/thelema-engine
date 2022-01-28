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

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.phys.IBoxShape
import kotlin.math.min

class ActionList(): ActionAdapter() {
    constructor(block: ActionList.() -> Unit): this() {
        block(this)
    }

    var customContext: IEntity? = null

    var isParallel: Boolean = false

    override val componentName: String
        get() = "ActionList"

    private val childActionsInternal: MutableList<IAction>
        get() = actionData.getTyped(0)

    val childActions: List<IAction>
        get() = actionData.getTyped(0)

    var currentAction: Int
        get() = actionData.getTyped(1)
        set(value) {
            actionData[1] = value
        }

    override var actionData: ActionData = ActionData(ArrayList<Action>(), 0)

    override val returnValue: Variable = Variable()

    override fun getContext(): IEntity? {
        return customContext ?: super.getContext()
    }

    override fun addedChildComponent(component: IEntityComponent) {
        if (component is IAction) childActionsInternal.add(component)
    }

    override fun removedChildComponent(component: IEntityComponent) {
        if (component is IAction) childActionsInternal.remove(component)
    }

    override fun restart() {
        for (i in childActions.indices) {
            childActions[i].restart()
        }
        isRunning = true
        currentAction = 0
    }

    private fun childrenIsRunning(): Boolean {
        for (i in childActions.indices) {
            if (childActions[i].isRunning) {
                return true
            }
        }
        return false
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            if (isParallel) {
                if (childrenIsRunning()) {
                    var remaining = delta
                    for (i in childActions.indices) {
                        remaining = min(childActions[i].update(delta), remaining)
                    }
                    if (remaining != 0f) {
                        isRunning = false
                        return remaining
                    } else if (!childrenIsRunning()) {
                        isRunning = false
                    }
                } else {
                    isRunning = false
                }
            } else {
                if (childActions.isNotEmpty()) {
                    val action = childActions.getOrNull(currentAction)
                    if (action != null) {
                        action.update(delta)

                        if (!action.isRunning) {
                            currentAction++
                            if (currentAction > childActions.lastIndex) {
                                currentAction = 0
                                isRunning = false
                            }
                        }
                    } else {
                        currentAction = if (currentAction < 0) 0 else childActions.lastIndex
                    }
                } else {
                    isRunning = false
                }
            }

        }
        return 0f
    }
}

fun IEntity.actionList(block: ActionList.() -> Unit) = component(block)
fun IEntity.actionList() = component<ActionList>()