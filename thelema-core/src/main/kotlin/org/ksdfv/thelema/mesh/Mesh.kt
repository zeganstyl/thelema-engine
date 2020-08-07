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

package org.ksdfv.thelema.mesh

import org.ksdfv.thelema.g3d.IMaterial
import org.ksdfv.thelema.g3d.Material
import org.ksdfv.thelema.gl.GL_TRIANGLES

/** @author zeganstyl */
class Mesh(
    override var vertices: IVertexBuffer? = null,
    override var indices: IIndexBufferObject? = null,
    override var material: IMaterial = Material(),
    override var instances: IVertexBuffer? = null,
    override var primitiveType: Int = GL_TRIANGLES
): IMesh {
    constructor(
        vertices: IVertexBuffer? = null,
        indices: IIndexBufferObject? = null,
        material: IMaterial = IMaterial.Default,
        instances: IVertexBuffer? = null,
        primitiveType: Int = GL_TRIANGLES,
        context: IMesh.() -> Unit
    ): this(
        vertices,
        indices,
        material,
        instances,
        primitiveType
    ) { context(this) }

    override var name: String = ""

    override fun copy(): IMesh = Mesh().set(this)
}