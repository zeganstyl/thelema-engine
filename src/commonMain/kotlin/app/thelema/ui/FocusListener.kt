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

package app.thelema.ui


/** Listener for [FocusEvent].
 * @author Nathan Sweet
 */
abstract class FocusListener : EventListener {
    override fun handle(event: Event): Boolean {
        if (event.eventType != EventType.Focus) return false
        event as FocusEvent
        when (event.focusEventType) {
            FocusEventType.keyboard -> keyboardFocusChanged(event, event.target!!, event.isFocused)
            FocusEventType.scroll -> scrollFocusChanged(event, event.target!!, event.isFocused)
        }
        return false
    }

    /** @param actor The event target, which is the actor that emitted the focus event. */
    open fun keyboardFocusChanged(event: FocusEvent, actor: Actor, focused: Boolean) {}

    /** @param actor The event target, which is the actor that emitted the focus event. */
    open fun scrollFocusChanged(event: FocusEvent, actor: Actor, focused: Boolean) {}
}
