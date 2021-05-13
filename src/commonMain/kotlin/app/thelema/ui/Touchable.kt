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

/** Determines how touch input events are distributed to an actor and any children.
 * @author Nathan Sweet
 */
enum class Touchable {
    /** All touch input events will be received by the actor and any children.  */
    Enabled,
    /** No touch input events will be received by the actor or any children.  */
    Disabled,
    /** No touch input events will be received by the actor, but children will still receive events. Note that events on the
     * children will still bubble to the parent.  */
    ChildrenOnly
}
