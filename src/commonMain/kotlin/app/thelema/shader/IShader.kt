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

package app.thelema.shader

import app.thelema.data.IFloatData
import app.thelema.g3d.IObject3D
import app.thelema.g3d.IScene
import app.thelema.gl.GL
import app.thelema.gl.IVertexAttribute
import app.thelema.math.*
import app.thelema.gl.IMesh
import app.thelema.shader.node.IShaderData
import app.thelema.shader.node.IShaderNode

/** @author zeganstyl */
interface IShader {
    /** GLSL version, by default is 110
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/Core_Language_(GLSL)#Version) */
    var version: Int

    /** If profile is empty, Core profile will be used.
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/OpenGL_Context#Context_types) */
    var profile: String

    /** User's vertex shader code. It must not include version directive #version. */
    val vertCode: String

    /** User's fragment shader code. It must not include version directive #version. */
    val fragCode: String

    var vertexShaderHandle: Int
    var fragmentShaderHandle: Int
    var programHandle: Int

    val log
        get() = if (isCompiled) GL.glGetProgramInfoLog(this.programHandle) else ""

    /** whether this program compiled successfully  */
    val isCompiled: Boolean

    /** Uniform lookup. Key - uniform name, value - location */
    val uniforms: MutableMap<String, Int>

    /** Attribute lookup. Key - vertex attribute name, value - location */
    val attributes: MutableMap<String, Int>

    val enabledAttributes: List<IVertexAttribute>

    /** From nodes can be generated source code */
    val nodes: MutableList<IShaderNode>

    var name: String

    /** Sometimes may be useful for custom shaders. For example, to bind textures before render mesh */
    var onMeshDraw: IShader.(mesh: IMesh) -> Unit

    /** For shader node GLSL-variables */
    fun getUID(data: IShaderData): String

    fun prepareSceneData(scene: IScene) {
        bind()

        for (i in nodes.indices) {
            val node = nodes[i]
            node.shaderOrNull = this
            node.prepareToDrawScene(scene)
        }
    }

    fun prepareObjectData(object3D: IObject3D) {
        bind()

        for (i in nodes.indices) {
            val node = nodes[i]
            node.shaderOrNull = this
            node.prepareObjectData(object3D)
        }
    }

    fun prepareToDrawMesh(mesh: IMesh) {
        bind()
        startTextureBinding()
        onMeshDraw(mesh)

        for (i in nodes.indices) {
            val node = nodes[i]
            node.shaderOrNull = this
            node.prepareToDrawMesh(mesh)
        }
    }

    fun setVertexAttributes(attributes: List<IVertexAttribute>) {
        attributes.forEach {
            val location = this.attributes[it.name]
            if (location != null) {
                GL.glEnableVertexAttribArray(location)
                GL.glVertexAttribPointer(location, it.size, it.type, it.normalized, it.stride, it.byteOffset)
            }
        }
    }

    fun enableAttribute(attribute: IVertexAttribute)

    fun disableAttribute(attribute: IVertexAttribute)

    fun disableAllAttributes()

    fun numerateLines(title: String, text: String, pad: Int = 5): String {
        val lines = text.split('\n')
        val str = StringBuilder(title.length + text.length + lines.size * 5)
        str.append(title)
        for (i in lines.indices) {
            str.append((i + 1).toString().padEnd(pad, ' '))
            str.append(lines[i])
            str.append("\n")
        }
        return str.toString()
    }

    /** Every line will be numerated */
    fun vertSourceCode(title: String = "=== VERTEX ===\n", pad: Int = 5): String = numerateLines(title, vertCode, pad)

    /** Every line will be numerated */
    fun fragSourceCode(title: String = "=== FRAGMENT ===\n", pad: Int = 5): String = numerateLines(title, fragCode, pad)

    /** Every line will be numerated */
    fun printCode(pad: Int = 5): String = vertSourceCode(pad = pad) + fragSourceCode(pad = pad)

    /** Makes this shader as current. */
    fun bind() {
        GL.glUseProgram(this.programHandle)
    }

    fun startTextureBinding() {
        GL.resetTextureUnitCounter()
    }

    fun getNextTextureUnit(): Int = GL.getNextTextureUnit()

    operator fun get(uniformName: String) = GL.glGetUniformLocation(programHandle, uniformName)

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



    /** @param pedantic flag indicating whether attributes & uniforms must be present at all times */
    fun getUniformLocation(name: String, pedantic: Boolean = false): Int {
        // -1 == not found
        var location = uniforms[name]
        if (location == null) {
            location = GL.glGetUniformLocation(this.programHandle, name)
            require(!(location == -1 && pedantic)) { "no uniform with name '$name' in shader" }
            uniforms[name] = location
        }
        return location
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun setUniformi(name: String, value: Int) {
        GL.glUniform1i(getUniformLocation(name), value)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int) {
        GL.glUniform2i(getUniformLocation(name), value1, value2)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int, value3: Int) {
        val location = getUniformLocation(name)
        GL.glUniform3i(location, value1, value2, value3)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int, value3: Int, value4: Int) =
        GL.glUniform4i(getUniformLocation(name), value1, value2, value3, value4)

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float) = GL.glUniform2f(getUniformLocation(name), value1, value2)

    /** Sets the vec3 uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float, value3: Float) =
        GL.glUniform3f(getUniformLocation(name), value1, value2, value3)

    /** Sets the vec4 uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float, value3: Float, value4: Float) =
        GL.glUniform4f(getUniformLocation(name), value1, value2, value3, value4)

    fun set1(name: String, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform1fv(getUniformLocation(name), length, values, offset)

    fun set2(name: String, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform2fv(getUniformLocation(name), length / 2, values, offset)

    fun set3(name: String, values: FloatArray, offset: Int = 0, length: Int = values.size) {
        val location = getUniformLocation(name)
        GL.glUniform3fv(location, length / 3, values, offset)
    }

    fun set4(name: String, values: FloatArray, offset: Int, length: Int) {
        val location = getUniformLocation(name)
        GL.glUniform4fv(location, length / 4, values, offset)
    }

    /** Sets the uniform matrix with the given name. Shader must be bound. */
    fun set(name: String, mat: IMat4, transpose: Boolean) {
        GL.glUniformMatrix4fv(getUniformLocation(name), 1, transpose, mat.values, 0)
    }

    /** Sets the uniform matrix with the given name. Shader must be bound. */
    fun set(name: String, mat: Mat3, transpose: Boolean = false) {
        GL.glUniformMatrix3fv(getUniformLocation(name), 1, transpose, mat.values, 0)
    }

    /** Sets an array of uniform matrices with the given name. Shader must be bound. */
    fun setUniformMatrix3fv(name: String, buffer: IFloatData, count: Int, transpose: Boolean) {
        buffer.position = 0
        val location = getUniformLocation(name)
        GL.glUniformMatrix3fv(location, count, transpose, buffer)
    }

    /** Sets an array of uniform matrices with the given name. Shader must be bound. */
    fun set(name: String, buffer: IFloatData, count: Int, transpose: Boolean) {
        buffer.position = 0
        val location = getUniformLocation(name)
        GL.glUniformMatrix4fv(location, count, transpose, buffer)
    }

    fun setMatrix4(name: String, values: FloatArray, offset: Int = 0, length: Int = values.size) =
        setMatrix4(getUniformLocation(name), values, offset, length)

    operator fun set(name: String, value: Boolean) = setUniformi(name, if (value) 1 else 0)

    operator fun set(name: String, value: Int) = setUniformi(name, value)

    operator fun set(name: String, value: Float) = GL.glUniform1f(getUniformLocation(name), value)

    /** Reduce value to float and set to uniform */
    operator fun set(name: String, value: Double) = GL.glUniform1f(getUniformLocation(name), value.toFloat())

    operator fun set(name: String, values: IVec2) = set(name, values.x, values.y)

    operator fun set(name: String, values: IVec3) = set(name, values.x, values.y, values.z)

    operator fun set(name: String, values: IVec4) = set(name, values.x, values.y, values.z, values.w)

    operator fun set(name: String, value: Mat3) = set(name, value, false)

    operator fun set(name: String, value: IMat4) = set(name, value, false)

    /** Sets the given attribute
     *
     * @param name the name of the attribute
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    fun setAttributef(name: String, value1: Float, value2: Float, value3: Float, value4: Float) =
        GL.glVertexAttrib4f(getAttributeLocation(name), value1, value2, value3, value4)

    fun getAttributeLocation(name: String) = attributes[name] ?: -1


    /** Generate source code from nodes */
    fun buildByNodes()

    /** Generate source code from nodes and compile shader */
    fun build()

    fun <T: IShaderNode> addNode(node: T): T {
        node.shaderOrNull = this
        nodes.add(node)
        return node
    }

    /** Disposes all resources associated with this shader. Must be called when the shader is no longer used.  */
    fun destroy() {
        GL.glUseProgram(0)
        GL.glDeleteShader(vertexShaderHandle)
        GL.glDeleteShader(fragmentShaderHandle)
        GL.glDeleteProgram(this.programHandle)

        attributes.clear()
        uniforms.clear()
    }
}