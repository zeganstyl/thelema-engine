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

package org.ksdfv.thelema.anim

/** Listener that will be informed when an animation is looped or completed.
 * @author Xoppa
 */
interface AnimListener {
    /** Gets called when an animation is completed.
     * @param animation The animation which just completed. */
    fun onEnd(animation: AnimPlayer.AnimationDesc) {}

    /** Gets called when an animation is looped.
     * @param animation The animation which just looped. */
    fun onLoop(animation: AnimPlayer.AnimationDesc) {}
}