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

import app.thelema.ecs.*

/** @author zeganstyl */
interface IAction: IEntityComponent {
    val isRunning: Boolean

    var actionData: ActionData

    val returnValue: Variable
        get() = Variable.Unknown

    fun createContext(out: MutableList<Pair<IAction, ActionData>>): MutableList<Pair<IAction, ActionData>> {
        out.add(Pair(this, actionData.copy()))
        return out
    }

    fun restart()

    fun resume()

    /** @return remaining time from [delta] after this action completed. By default return 0 */
    fun update(delta: Float): Float

    fun getContext(): IEntity? =
        entityOrNull?.parentEntity?.componentOrNull<IAction>()?.getContext()

    fun <T: IAction> action(actionComponentName: String, entityName: String? = null, block: T.() -> Unit): T {
        return if (entityName == null) {
            val child = Entity()
            child.name = actionComponentName
            getOrCreateEntity().addEntity(child)
            child.componentTyped<T>(actionComponentName).apply(block)
        } else {
            getOrCreateEntity().entity(entityName).componentTyped<T>(actionComponentName).apply(block)
        }
    }
}

fun IEntity.action(block: IAction.() -> Unit) = component(block)
fun IEntity.action() = component<IAction>()