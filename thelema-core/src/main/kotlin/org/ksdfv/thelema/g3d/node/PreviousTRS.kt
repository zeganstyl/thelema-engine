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

package org.ksdfv.thelema.g3d.node

import org.ksdfv.thelema.math.IMat4

/** Stores only world matrix
 *
 * @author zeganstyl */
class PreviousTRS: AdapterTransformNode() {
    override val nodeType: Int
        get() = TransformNodeType.TRS

    override val worldMatrix: IMat4 = IMat4.Build()

    override fun copy(): ITransformNode = PreviousTRS().set(this)

    override fun set(other: ITransformNode): PreviousTRS {
        worldMatrix.set(other.worldMatrix)
        return this
    }
}