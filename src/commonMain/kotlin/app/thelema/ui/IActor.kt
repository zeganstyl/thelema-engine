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

import app.thelema.g2d.Batch
import app.thelema.math.IVec2

interface IActor {
    /** Stage that this actor is currently in, or null if not in a stage. */
    var headUpDisplay: HeadUpDisplay?

    /** Parent actor, or null if not in a group.  */
    var parent: Group?

    val globalPosition: IVec2

    var x: Float
    var y: Float

    var width: Float
    var height: Float

    fun updateTransform(recursive: Boolean = true) {
        val parent = parent
        if (parent != null) {
            globalPosition.set(parent.globalPosition.x + x, parent.globalPosition.y + y)
        } else {
            globalPosition.set(x, y)
        }
    }

    /** Draws the actor. The batch is configured to draw in the parent's coordinate system.
     * [Batch.draw] is convenient to draw a rotated and scaled TextureRegion. [Batch.begin] has already been called on
     * the batch. If [Batch.end] is called to draw without the batch then [Batch.begin] must be called before the
     * method returns.
     *
     *
     * @param parentAlpha The parent alpha, to be multiplied with this actor's alpha, allowing the parent's alpha to affect all
     * children.
     */
    fun draw(batch: Batch, parentAlpha: Float) {}
}