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

import app.thelema.ecs.ComponentCanBeRebuild
import app.thelema.ecs.IEntityComponent
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexAccessor
import app.thelema.gl.IVertexAttribute
import app.thelema.shader.IShader
import app.thelema.shader.useShader

interface IMeshBuilder: IEntityComponent, ComponentCanBeRebuild {
    val mesh: IMesh

    fun getVerticesCount(): Int

    fun getIndicesCount(): Int

    fun applyVertices()

    fun applyIndices()

    fun preparePositions(block: IVertexAccessor.() -> Unit)
    fun prepareUvs(block: IVertexAccessor.() -> Unit)
    fun prepareNormals(block: IVertexAccessor.() -> Unit)

    fun prepareIndices(block: IIndexBuffer.() -> Unit)

    fun render(shader: IShader) {
        shader.useShader { mesh.render() }
    }

    override fun rebuildComponent() {
        updateMesh()
    }

    fun updateMesh()

    fun requestMeshUpdate() {
        requestRebuild()
    }
}