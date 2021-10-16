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

import app.thelema.ecs.IEntityComponent
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexAttribute
import app.thelema.shader.IShader

interface IMeshBuilder: IEntityComponent {
    val mesh: IMesh

    var isMeshUpdateRequested: Boolean

    fun getVerticesCount(): Int

    fun getIndicesCount(): Int

    fun applyVertices()

    fun applyIndices()

    fun preparePositions(block: IVertexAttribute.() -> Unit)
    fun prepareUvs(block: IVertexAttribute.() -> Unit)
    fun prepareNormals(block: IVertexAttribute.() -> Unit)

    fun prepareIndices(block: IIndexBuffer.() -> Unit)

    fun render(shader: IShader) {
        mesh.render(shader)
    }

    fun updateMesh()

    fun requestMeshUpdate() {
        isMeshUpdateRequested = true
    }
}