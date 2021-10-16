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

package app.thelema.g3d.mesh

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexAttribute
import app.thelema.json.IJsonObject

abstract class MeshBuilderAdapter: IMeshBuilder {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            builder = value?.component() ?: MeshBuilder()
            builder.proxy = this
        }

    open var builder = MeshBuilder()

    override val mesh: IMesh
        get() = builder.mesh

    override var isMeshUpdateRequested: Boolean
        get() = builder.isMeshUpdateRequested
        set(value) { builder.isMeshUpdateRequested = value }

    override fun preparePositions(block: IVertexAttribute.() -> Unit) = builder.preparePositions(block)
    override fun prepareUvs(block: IVertexAttribute.() -> Unit) = builder.prepareUvs(block)
    override fun prepareNormals(block: IVertexAttribute.() -> Unit) = builder.prepareNormals(block)

    override fun prepareIndices(block: IIndexBuffer.() -> Unit) = builder.prepareIndices(block)

    override fun updateMesh() {
        builder.updateMesh()
    }
}