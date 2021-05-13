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

package app.thelema.gl

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.getComponentOrNull
import app.thelema.g3d.IMaterial
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.shader.IShader

/** @author zeganstyl */
class Mesh(): IMesh {
    constructor(block: Mesh.() -> Unit): this() {
        block(this)
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (material == null) material = value?.getComponentOrNull()
        }

    override var indices: IIndexBuffer? = null
    override var material: IMaterial? = null
    override var instances: IVertexBuffer? = null
    override var primitiveType: Int = GL_TRIANGLES

    override var verticesCount: Int = 0
    override val vertexBuffers: MutableList<IVertexBuffer> = ArrayList()
    override var vaoHandle: Int = -1
    override var instancesCountToRender: Int = -1

    override val componentName: String
        get() = "Mesh"

    override val centroid: IVec3 = Vec3()

    override fun addVertexBuffer(buffer: IVertexBuffer) {
        vertexBuffers.add(buffer)
        verticesCount = buffer.verticesCount
    }

    override fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer {
        val buffer = VertexBuffer()
        block(buffer)
        addVertexBuffer(buffer)
        return buffer
    }

    override fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer {
        val buffer = IndexBuffer()
        block(buffer)
        indices = buffer
        return buffer
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other.componentName == componentName && other != this) set(other as IMesh)
        return this
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMaterial && material == null) material = component
    }

    override fun render(shader: IShader, offset: Int, count: Int) {
        if (count == 0) return

        bind(shader)

        val numInstances = instancesCountToRender

        val indices = indices
        if (indices != null && indices.bytes.limit > 0) {
            if (count + offset > indices.bytes.limit) {
                throw RuntimeException("Mesh attempting to access memory outside of the index buffer (count: "
                        + count + ", offset: " + offset + ", max: " + indices.bytes.limit + ")")
            }

            if (indices.bufferHandle == 0) return

            if (numInstances < 0) {
                shader.prepareToDrawMesh(this)
                GL.glDrawElements(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex)
            } else if (numInstances > 0) {
                shader.prepareToDrawMesh(this)
                GL.glDrawElementsInstanced(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex, numInstances)
            }
        } else {
            if (numInstances < 0) {
                shader.prepareToDrawMesh(this)
                GL.glDrawArrays(primitiveType, offset, count)
            } else if (numInstances > 0) {
                shader.prepareToDrawMesh(this)
                GL.glDrawArraysInstanced(primitiveType, offset, count, numInstances)
            }
        }
    }

    override fun destroy() {
        if (vaoHandle > 0) {
            GL.glDeleteVertexArrays(vaoHandle)
            vaoHandle = 0
        }

        vertexBuffers.forEach { it.destroy() }
        vertexBuffers.clear()

        indices?.destroy()
        indices = null
    }
}
