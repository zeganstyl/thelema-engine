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

/** @author zeganstyl */
class ActionSequence(): IActionContainer {
    constructor(vararg children: IAction): this() {
        this.children.addAll(children)
    }

    constructor(context: ActionSequence.() -> Unit): this() { context(this) }

    override var isRunning: Boolean = true

    override val children: MutableList<IAction> = ArrayList()

    var currentAction: Int = 0

    override fun reset() {
        super.reset()
        isRunning = true
        currentAction = 0
    }

    override fun update(delta: Float) {
        if (isRunning) {
            if (children.size > 0) {
                val action = children[currentAction]
                action.update(delta)

                if (!action.isRunning) {
                    currentAction++
                    if (currentAction > children.lastIndex) {
                        currentAction = 0
                        isRunning = false
                    }
                }
            } else {
                isRunning = false
            }
        }
    }
}