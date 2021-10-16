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

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull

/** Main action of entity. */
class Action: IAction {
    override var entityOrNull: IEntity? = null

    override val isRunning: Boolean
        get() = proxy?.isRunning ?: false

    var proxy: IAction?
        get() = actionData.getTyped(0)
        set(value) { actionData[0] = value }

    override val componentName: String
        get() = "Action"

    override var actionData: ActionData = ActionData(null)

    override val returnValue: Variable
        get() = proxy?.returnValue ?: Variable.Unknown

    override fun resume() {
        proxy?.resume()
    }

    override fun createContext(out: MutableList<Pair<IAction, ActionData>>): MutableList<Pair<IAction, ActionData>> {
        super.createContext(out)
        proxy?.createContext(out)
        entity.forEachChildEntity {
            it.componentOrNull<IAction>()?.createContext(out)
        }
        return out
    }

    override fun restart() {
        proxy?.restart()
    }

    override fun update(delta: Float): Float {
        return proxy?.update(delta) ?: 0f
    }

    override fun getContext(): IEntity? = proxy?.getContext()

    companion object {
        fun setupActionComponents() {
            ECS.descriptor({ Action() }) {
                setAliases(IAction::class)

                descriptor { ActionList() }
                descriptor({ DelayAction() }) {
                    float("delay", { delay }) { delay = it }
                }
                descriptor({ MoveToTargetAction() }) {
                    float("speed", { speed }) { speed = it }
                    ref("target", { target }) { target = it }
                }
                descriptor({ MoveForwardAction() }) {
                    float("length", { length }) { length = it }
                }
                descriptor({ RotateYAction() }) {
                    float("angleLength", { angleLength }) { angleLength = it }
                }
                descriptor { SwitchAction() }
                descriptor { BiOperation() }
                descriptor { VariableAction() }
            }
        }
    }
}