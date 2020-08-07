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
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_TRIANGLES
import org.ksdfv.thelema.shader.IShader

/** Mesh is vertex buffer (+ index buffer) (+ instanced buffer) + material.
 *
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Xoppa, zeganstyl
 * */
interface IMesh {
    var primitiveType: Int
        get() = GL_TRIANGLES
        set(_) = Unit

    var vertices: IVertexBuffer?

    var indices: IIndexBufferObject?

    /** Instanced buffer https://www.khronos.org/opengl/wiki/Vertex_Rendering#Instancing */
    var instances: IVertexBuffer?

    var material: IMaterial

    var name: String

    /** Binds the underlying [VertexBufferObject] and [IndexBufferObject] if indices where given. Use this with OpenGL
     * ES 2.0 and when auto-bind is disabled.
     *
     * @param shader the shader (does not bind the shader)
     */
    fun bind(shader: IShader) {
        vertices?.bind(shader)
        instances?.bind(shader)
        if (indices?.sizeInBytes ?: 0 > 0) indices?.bind()
    }

    /**
     * Renders the mesh using the given primitive type. offset specifies the offset into either the vertex buffer or the index
     * buffer depending on whether indices are defined. count specifies the number of vertices or indices to use thus count /
     * #vertices per primitive primitives are rendered.
     * This method will automatically bind each vertex attribute as specified at construction time via [VertexInputs] to
     * the respective shader attributes. The binding is based on the alias defined for each [IVertexInput].
     *
     * @param shader the shader to be used
     * @param primitiveType the primitive type
     * @param offset the offset into the vertex or index buffer
     * @param count number of vertices or indices to use
     */
    fun render(
        shader: IShader,
        primitiveType: Int = this.primitiveType,
        offset: Int = 0,
        count: Int = (if (indices?.sizeInBytes ?: 0 > 0) indices?.size else vertices?.size) ?: 0,
        bind: Boolean = autoBind
    ) {
        if (count == 0) return
        if (vertices?.handle == 0) return

        if (bind) bind(shader)

        val instances = instances
        val numInstances = instances?.instancesToRender ?: 0

        val indices = indices
        if (indices != null && indices.sizeInBytes > 0) {
            if (count + offset > indices.sizeInBytes) {
                throw RuntimeException("Mesh attempting to access memory outside of the index buffer (count: "
                        + count + ", offset: " + offset + ", max: " + indices.sizeInBytes + ")")
            }

            if (indices.handle == 0) return

            if (instances != null) {
                if (numInstances > 0) {
                    if (instances.handle == 0) return
                    shader.prepareToDrawMesh(this)
                    GL.glDrawElementsInstanced(primitiveType, count, indices.type, offset * indices.bytesPerIndex, numInstances)
                }
            } else {
                shader.prepareToDrawMesh(this)
                GL.glDrawElements(primitiveType, count, indices.type, offset * indices.bytesPerIndex)
            }
        } else {
            if (instances != null) {
                if (numInstances > 0) {
                    if (instances.handle == 0) return
                    shader.prepareToDrawMesh(this)
                    GL.glDrawArraysInstanced(primitiveType, offset, count, numInstances)
                }
            } else {
                shader.prepareToDrawMesh(this)
                GL.glDrawArrays(primitiveType, offset, count)
            }
        }
    }

    fun set(other: IMesh): IMesh {
        vertices = other.vertices
        indices = other.indices
        instances = other.instances
        material = other.material
        primitiveType = other.primitiveType
        return this
    }

    fun copy(): IMesh

    /** All linked buffers will be destroyed */
    fun destroy() {
        vertices?.destroy()
        indices?.destroy()
        instances?.destroy()
        vertices = null
        indices = null
        instances = null
        material = IMaterial.Default
    }

    companion object {
        /** Sets whether to bind the underlying [vertices] automatically on a call to one of the
         * render methods. Usually you want to use autoBind. Manual binding is an expert functionality. There is a driver bug on the
         * MSM720xa chips that will fuck up memory if you manipulate the vertices and indices of a Mesh multiple times while it is
         * bound. Keep this in mind.
         */
        var autoBind: Boolean = true

        var Build: () -> IMesh = { Mesh(material = IMaterial.Default) }
    }
}