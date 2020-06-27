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

import org.ksdfv.thelema.ext.contains
import org.ksdfv.thelema.ext.traverseSafe

/** @author zeganstyl */
class ParallelActions(): IActionContainer {
    constructor(vararg children: IAction): this() {
        this.children.addAll(children)
    }

    constructor(context: ParallelActions.() -> Unit): this() { context(this) }

    override var isRunning: Boolean = true

    override val children: MutableList<IAction> = ArrayList()

    override fun reset() {
        super.reset()
        isRunning = true
    }

    override fun update(delta: Float) {
        if (isRunning) {
            if (children.size > 0 && children.contains(containsCall)) {
                children.traverseSafe { it.update(delta) }
            } else {
                isRunning = false
            }
        }
    }

    companion object {
        private val containsCall: (act: IAction) -> Boolean = { it.isRunning }
    }
}