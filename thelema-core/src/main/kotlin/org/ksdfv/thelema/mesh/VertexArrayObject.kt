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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_ARRAY_BUFFER
import org.ksdfv.thelema.gl.GL_STATIC_DRAW
import org.ksdfv.thelema.shader.IShader

/**
 * <p>
 * A {@link VertexData} implementation that uses vertex buffer objects and vertex array objects.
 * (This is required for OpenGL 3.0+ core profiles. In particular, the default VAO has been
 * deprecated, as has the use of client memory for passing vertex attributes.) Use of VAOs should
 * give a slight performance benefit since you don't have to bind the attributes on every draw
 * anymore.
 * </p>
 *
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * </p>
 *
 * <p>
 * VertexBufferObjectWithVAO objects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 *
 * Code adapted from {@link VertexBufferObject}.
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Nate Austin <nate.austin gmail>, zeganstyl
 */
class VertexArrayObject(
    override var attributes: IVertexInputs,
    byteBuffer: IByteData,
    override var usage: Int = GL_STATIC_DRAW,
    initGpuObjects: Boolean = true,
    loadBufferToGpu: Boolean = true
) : IVertexBuffer {

    override var floatBuffer: IFloatData = byteBuffer.floatView()

    override var bytes = byteBuffer
        set(value) {
            field = value
            isBufferNeedReload = true
            floatBuffer = value.floatView()
        }

    override var handle = 0
    private var vaoHandle = 0

    override var isBufferNeedReload = true
    override var isBound = false

    /** Key - vertex attribute id, value - location id */
    private var cachedAttributeToLocation = HashMap<String, Int>()

    override var instancesToRenderCount: Int = 0

    init {
        if (initGpuObjects) {
            initGpuObjects()
            if (loadBufferToGpu) bind(null)
        }
    }

    override fun initGpuObjects() {
        handle = GL.glGenBuffer()

        vaoHandle = GL.glGenVertexArrays()
    }

    override fun bind(shader: IShader?) {
        GL.glBindVertexArray(vaoHandle)
        isBound = true

        if (shader != null) bindAttributes(shader)

        if (isBufferNeedReload) loadBufferToGpu()
    }

    override fun loadBufferToGpu() {
        if (isBound) {
            GL.glBindBuffer(GL_ARRAY_BUFFER, handle)
            GL.glBufferData(GL_ARRAY_BUFFER, bytes.size, bytes, usage)
            isBufferNeedReload = false
        }
    }

    private fun bindAttributes(shader: IShader) {
        var stillValid = cachedAttributeToLocation.size != 0

        if (stillValid) {
            stillValid = shader.attributeLocations.size == cachedAttributeToLocation.size &&
                    attributes.firstOrNull { shader.attributeLocations[it.name] == cachedAttributeToLocation[it.name] } == null
        }

        if (!stillValid) {
            GL.glBindBuffer(GL_ARRAY_BUFFER, handle)
            unbindAttributes(shader)
            cachedAttributeToLocation.clear()

            attributes.forEach {
                val location = shader.attributeLocations[it.name]
                if (location != null) {
                    cachedAttributeToLocation[it.name] = location

                    shader.setVertexAttributes(attributes)
                }
            }
        }
    }

    private fun unbindAttributes(shader: IShader) {
        if (cachedAttributeToLocation.size == 0) {
            return
        }

        attributes.forEach {
            val location = cachedAttributeToLocation[it.name]
            if (location != null) {
                GL.glDisableVertexAttribArray(location)
            }
        }
    }

    override fun unbind(shader: IShader?) {
        val gl = GL
        gl.glBindVertexArray(0)
        isBound = false
    }

    override fun destroy() {
        if (handle != 0) {
            GL.glBindBuffer(GL_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(handle)
            handle = 0
        }

        if (vaoHandle != 0) {
            GL.glDeleteVertexArrays(vaoHandle)
            vaoHandle = 0
        }
    }
}
