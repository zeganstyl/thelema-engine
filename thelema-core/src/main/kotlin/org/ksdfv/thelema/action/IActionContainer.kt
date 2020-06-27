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

import org.ksdfv.thelema.ext.traverseSafe

/** @author zeganstyl */
interface IActionContainer: IAction {
    val children: MutableList<IAction>

    fun add(action: IAction): IActionContainer {
        children.add(action)
        return this
    }

    fun remove(action: IAction): IActionContainer {
        children.remove(action)
        return this
    }

    fun lambda(updateCall: (delta: Float) -> Boolean): LambdaDelegateAction {
        val action = LambdaDelegateAction(updateCall)
        children.add(action)
        return action
    }

    fun sequence(context: ActionSequence.() -> Unit): ActionSequence {
        val action = ActionSequence(context)
        children.add(action)
        return action
    }

    fun sequence(vararg actions: IAction): ActionSequence {
        val action = ActionSequence(*actions)
        children.add(action)
        return action
    }

    fun parallel(context: ParallelActions.() -> Unit): ParallelActions {
        val action = ParallelActions(context)
        children.add(action)
        return action
    }

    fun parallel(vararg actions: IAction): ParallelActions {
        val action = ParallelActions(*actions)
        children.add(action)
        return action
    }

    fun lerpFloat(
        speed: Float,
        target: Float,
        getValue: () -> Float,
        setValue: (newValue: Float) -> Unit
    ): LerpFloatToTargetWithSpeed {
        val action = LerpFloatToTargetWithSpeed(speed, target, getValue, setValue)
        children.add(action)
        return action
    }

    /** Delay before running next action */
    fun delay(delay: Float): DelayAction {
        val action = DelayAction(delay)
        children.add(action)
        return action
    }

    /** Run some function */
    fun run(call: () -> Unit): RunAction {
        val action = RunAction(call)
        children.add(action)
        return action
    }

    /** Firstly delay, then run */
    fun delayRun(delay: Float, call: () -> Unit): DelayRunAction {
        val action = DelayRunAction(delay, call)
        children.add(action)
        return action
    }

    /** Firstly fun, then delay */
    fun runDelay(delay: Float, call: () -> Unit): RunDelayAction {
        val action = RunDelayAction(delay, call)
        children.add(action)
        return action
    }

    override fun reset() {
        children.traverseSafe { it.reset() }
    }
}