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

package org.ksdfv.thelema.shader

import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.mesh.IVertexInputs

/** @author zeganstyl */
interface IShader {
    /** GLSL version, by default is 110
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/Core_Language_(GLSL)#Version) */
    var version: Int

    /** If profile is empty, Core profile will be used.
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/OpenGL_Context#Context_types) */
    var profile: String

    val vertCode: String
    val fragCode: String

    var vertexShaderHandle: Int
    var fragmentShaderHandle: Int
    var handle: Int

    val log
        get() = if (isCompiled) GL.glGetProgramInfoLog(this.handle) else ""

    /** whether this program compiled successfully  */
    val isCompiled: Boolean

    /** Key - vertex attribute id, value - location */
    val attributeLocations: MutableMap<String, Int>

    fun prepareSceneData(scene: IScene) {
        bind()
    }

    fun prepareObjectData(object3D: IObject3D) {
        bind()
    }

    fun prepareToDrawMesh(mesh: IMesh) {
        bind()
    }

    fun setVertexAttributes(attributes: IVertexInputs) {
        attributes.forEach {
            val location = attributeLocations[it.name]
            if (location != null) {
                GL.glEnableVertexAttribArray(location)
                GL.glVertexAttribPointer(location, it.size, it.type, it.normalized, attributes.bytesPerVertex, it.byteOffset)
            }
        }
    }

    fun numerateLines(title: String, text: String, pad: Int = 5): String {
        val lines = text.split('\n')
        val str = StringBuilder(title.length + text.length + lines.size * 5)
        str.append(title)
        for (i in lines.indices) {
            str.append((i + 1).toString().padEnd(pad, ' '))
            str.appendln(lines[i])
        }
        return str.toString()
    }

    /** Every line will be numerated */
    fun vertSourceCode(title: String = "=== VERTEX ===\n", pad: Int = 5): String = numerateLines(title, vertCode, pad)

    /** Every line will be numerated */
    fun fragSourceCode(title: String = "=== FRAGMENT ===\n", pad: Int = 5): String = numerateLines(title, fragCode, pad)

    /** Every line will be numerated */
    fun sourceCode(pad: Int = 5): String = vertSourceCode(pad = pad) + fragSourceCode(pad = pad)

    fun updateShader() {}

    /** Makes this shader as current. */
    fun bind() {
        GL.glUseProgram(this.handle)
    }

    operator fun get(uniformName: String) = GL.glGetUniformLocation(handle, uniformName)

    fun set(location: Int, value1: Int, value2: Int) = GL.glUniform2i(location, value1, value2)

    fun set(location: Int, value1: Int, value2: Int, value3: Int) = GL.glUniform3i(location, value1, value2, value3)

    fun set(location: Int, value1: Int, value2: Int, value3: Int, value4: Int) =
        GL.glUniform4i(location, value1, value2, value3, value4)

    fun set(location: Int, value1: Float, value2: Float) = GL.glUniform2f(location, value1, value2)

    fun set(location: Int, value1: Float, value2: Float, value3: Float) =
        GL.glUniform3f(location, value1, value2, value3)

    fun set(location: Int, value1: Float, value2: Float, value3: Float, value4: Float) =
        GL.glUniform4f(location, value1, value2, value3, value4)

    fun set1(location: Int, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform1fv(location, length, values, offset)

    fun set2(location: Int, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform2fv(location, length / 2, values, offset)

    fun set3(location: Int, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform3fv(location, length / 3, values, offset)

    fun set4(location: Int, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform4fv(location, length / 4, values, offset)

    fun set(location: Int, mat: IMat4, transpose: Boolean) =
        GL.glUniformMatrix4fv(location, 1, transpose, mat.values, 0)

    fun set(location: Int, mat: Mat3, transpose: Boolean) =
        GL.glUniformMatrix3fv(location, 1, transpose, mat.values, 0)

    fun setMatrix3(location: Int, values: FloatArray, offset: Int = 0, length: Int = values.size) {
        GL.glUniformMatrix3fv(location, length / 9, false, values, offset)
    }

    fun setMatrix4(location: Int, values: FloatArray, offset: Int = 0, length: Int = values.size) {
        GL.glUniformMatrix4fv(location, length / 16, false, values, offset)
    }

    operator fun set(location: Int, value: Boolean) = GL.glUniform1i(location, if (value) 1 else 0)

    operator fun set(location: Int, value: Int) = GL.glUniform1i(location, value)

    operator fun set(location: Int, value: Float) = GL.glUniform1f(location, value)

    /** Reduce value to float and set to uniform */
    operator fun set(location: Int, value: Double) = GL.glUniform1f(location, value.toFloat())

    operator fun set(location: Int, values: IVec2) = set(location, values.x, values.y)

    operator fun set(location: Int, values: IVec3) = set(location, values.x, values.y, values.z)

    operator fun set(location: Int, values: IVec4) = set(location, values.x, values.y, values.z, values.w)

    operator fun set(location: Int, value: Mat3) = set(location, value, false)

    operator fun set(location: Int, value: IMat4) = set(location, value, false)


    /** Disposes all resources associated with this shader. Must be called when the shader is no longer used.  */
    fun destroy() {
        GL.glUseProgram(0)
        GL.glDeleteShader(vertexShaderHandle)
        GL.glDeleteShader(fragmentShaderHandle)
        GL.glDeleteProgram(this.handle)
    }
}