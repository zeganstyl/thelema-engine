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

/** Fired when an actor gains or loses keyboard or scroll focus. Can be cancelled to prevent losing or gaining focus.
 * @author Nathan Sweet
 */
class FocusEvent : Event(EventType.Focus) {
    var isFocused = false
    var focusEventType: FocusEventType? = null
    /** The actor related to the event. When focus is lost, this is the new actor being focused, or null. When focus is gained,
     * this is the previous actor that was focused, or null.  */
    var relatedActor: Actor? = null

    override fun reset() {
        super.reset()
        relatedActor = null
    }
}