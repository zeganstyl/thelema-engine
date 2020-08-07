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

import org.ksdfv.thelema.math.*

/** Translation only node. This node does not store matrices, only vec3 for position
 *
 * @author zeganstyl */
class TranslationNode: ITransformNode {
    override var name: String = ""

    override val position: IVec3 = Vec3()

    override val rotation: IVec4
        get() = IVec4.Zero3One1

    override val scale: IVec3
        get() = IVec3.One

    override val worldMatrix: IMat4
        get() = tmp.setToTranslation(position)

    override var parentNode: ITransformNode? = null

    override val childNodes: List<ITransformNode> = ArrayList()

    override val nodeType: Int
        get() = TransformNodeType.Translation

    override fun set(other: ITransformNode): TranslationNode {
        super.set(other)
        position.set(other.position)
        return this
    }

    override fun copy(): ITransformNode = TranslationNode().set(this)

    companion object {
        val tmp = Mat4()
    }
}