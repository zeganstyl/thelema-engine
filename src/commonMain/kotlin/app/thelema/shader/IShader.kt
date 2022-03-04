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
import app.thelema.ecs.Entity
import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IUniformArgs
import app.thelema.gl.*
import app.thelema.math.*
import app.thelema.shader.node.IRootShaderNode
import app.thelema.shader.node.IShaderNode

/** @author zeganstyl */
interface IShader: IEntityComponent {
    /** GLSL version, by default is 110
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/Core_Language_(GLSL)#Version) */
    var version: Int

    var floatPrecision: String

    /** If profile is empty, Core profile will be used.
     * [OpenGL documentation](https://www.khronos.org/opengl/wiki/OpenGL_Context#Context_types) */
    var profile: String

    /** User's vertex shader code. It must not include version directive #version. */
    val vertCode: String

    /** User's fragment shader code. It must not include version directive #version. */
    val fragCode: String

    val log: String

    /** whether this program compiled successfully  */
    val isCompiled: Boolean

    /** Uniform lookup. Key - uniform name, value - location */
    val uniforms: MutableMap<String, Int>

    /** Attribute lookup. Key - vertex attribute name, value - location */
    val attributes: MutableMap<String, Int>

    val enabledAttributes: List<IVertexAccessor>

    val nodes: Array<IShaderNode>

    var depthMask: Boolean

    var rootNode: IRootShaderNode?

    var buildRequested: Boolean

    var vertexLayout: IVertexLayout

    var uniformArgs: IUniformArgs?

    var listener: ShaderListener?

    fun onBind(block: IShader.() -> Unit): ShaderListener {
        listener = object : ShaderListener {
            override fun bind(shader: IShader) {
                block(shader)
            }
        }
        return listener!!
    }

    fun findShaderNode(componentName: String): IShaderNode?

    fun load(vertCode: String, fragCode: String)

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
    fun bind()

    fun startTextureBinding() {
        GL.resetTextureUnitCounter()
    }

    fun getNextTextureUnit(): Int = GL.getNextTextureUnit()

    operator fun get(uniformName: String): Int

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

    /** @param count matrices count */
    fun setMatrix3(location: Int, values: FloatArray, offset: Int, count: Int, transpose: Boolean = false) {
        GL.glUniformMatrix3fv(location, count, transpose, values, offset)
    }

    /** @param count matrices count */
    fun setMatrix4(location: Int, values: FloatArray, offset: Int, count: Int, transpose: Boolean = false) {
        GL.glUniformMatrix4fv(location, count, transpose, values, offset)
    }

    operator fun set(location: Int, value: Boolean) = GL.glUniform1i(location, if (value) 1 else 0)

    operator fun set(location: Int, value: Int) = GL.glUniform1i(location, value)

    operator fun set(location: Int, value: Float) = GL.glUniform1f(location, value)

    /** Reduce value to float and set to uniform */
    operator fun set(location: Int, value: Double) = GL.glUniform1f(location, value.toFloat())

    operator fun set(location: Int, values: IVec2) = set(location, values.x, values.y)

    operator fun set(location: Int, values: IVec3C) = set(location, values.x, values.y, values.z)

    operator fun set(location: Int, values: IVec4C) = set(location, values.x, values.y, values.z, values.w)

    operator fun set(location: Int, value: Mat3) = set(location, value, false)

    operator fun set(location: Int, value: IMat4) = set(location, value, false)



    /** @param pedantic flag indicating whether attributes & uniforms must be present at all times */
    fun getUniformLocation(name: String, pedantic: Boolean = false): Int

    fun hasUniform(name: String): Boolean

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

    fun setMatrix3(name: String, values: FloatArray, offset: Int, count: Int, transpose: Boolean = false) =
        setMatrix3(getUniformLocation(name), values, offset, count, transpose)

    fun setMatrix4(name: String, values: FloatArray, offset: Int, count: Int, transpose: Boolean = false) =
        setMatrix4(getUniformLocation(name), values, offset, count, transpose)

    operator fun set(name: String, value: Boolean) = setUniformi(name, if (value) 1 else 0)

    operator fun set(name: String, value: Int) = setUniformi(name, value)

    /** Set vec4 color from RGBA8888 integer value */
    fun setColor(name: String, rgba8888: Int) = set(name,
        (rgba8888 and -0x1000000 ushr 24) * inv255,
        (rgba8888 and 0x00ff0000 ushr 16) * inv255,
        (rgba8888 and 0x0000ff00 ushr 8) * inv255,
        (rgba8888 and 0x000000ff) * inv255
    )

    operator fun set(name: String, value: Float) = GL.glUniform1f(getUniformLocation(name), value)

    /** Reduce value to float and set to uniform */
    operator fun set(name: String, value: Double) = GL.glUniform1f(getUniformLocation(name), value.toFloat())

    operator fun set(name: String, values: IVec2) = set(name, values.x, values.y)

    operator fun set(name: String, values: IVec3C) = set(name, values.x, values.y, values.z)

    operator fun set(name: String, values: IVec4C) = set(name, values.x, values.y, values.z, values.w)

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


    /** Generate source code from nodes and compile shader */
    fun build()

    fun requestBuild()

    fun <T: IShaderNode> addNode(node: T): T {
        entityOrNull?.addEntity(Entity(node.componentName) { addComponent(node) })
        return node
    }

    companion object {
        private const val inv255 = 1f / 255f
    }
}

inline fun <reified T> IShader.findShaderNode(): T? = findShaderNode(T::class.simpleName!!) as T?

inline fun <reified T> IShader.findShaderNode(block: T.() -> Unit): T? =
    (findShaderNode(T::class.simpleName!!) as T?)?.apply(block)

/** Create shader node and add it to entity */
inline fun <reified T: IShaderNode> IShader.node(block: T.() -> Unit): T {
    val componentName = T::class.simpleName!!
    val entity = Entity(componentName)
    getOrCreateEntity().addEntity(entity)
    val component = entity.addComponent(componentName) as T
    block(component)
    return component
}

inline fun IShader.useShader(block: IShader.() -> Unit) {
    bind()
    block()
}