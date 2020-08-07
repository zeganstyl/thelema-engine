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
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4

/** @author zeganstyl */
open class AdapterTransformNode: ITransformNode {
    override var name: String
        get() = ""
        set(_) {}

    override val position: IVec3
        get() = IVec3.Zero

    override val rotation: IVec4
        get() = IVec4.Zero3One1

    override val scale: IVec3
        get() = IVec3.One

    override val worldMatrix: IMat4
        get() = IMat4.Cap

    override var parentNode: ITransformNode?
        get() = null
        set(_) {}

    override val childNodes: List<ITransformNode>
        get() = childNodesCap

    override val nodeType: Int
        get() = TransformNodeType.None

    override fun copy(): ITransformNode = AdapterTransformNode()

    companion object {
        val childNodesCap = ArrayList<ITransformNode>()
    }
}