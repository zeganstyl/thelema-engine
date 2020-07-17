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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
open class Shader(
    override var vertCode: String = "",
    override var fragCode: String = "",
    var name: String = "",
    compile: Boolean = true,

    /** Precision will be added as first line */
    var defaultPrecision: String = if (APP.platformType != APP.Desktop) "precision mediump float;\n" else "",
    override var version: Int = 110,
    override var profile: String = ""
): IShader {
    /** the log  */
    protected var logInternal = ""
    override val log
        get() = if (isCompiled) GL.glGetProgramInfoLog(this.handle) else logInternal

    protected var isCompiledInternal: Boolean = false
    override val isCompiled: Boolean
        get() = isCompiledInternal

    override val uniforms = HashMap<String, Int>()
    override val attributes = HashMap<String, Int>()

    override var vertexShaderHandle: Int = 0
    override var fragmentShaderHandle: Int = 0
    override var handle: Int = 0

    init {
        if (compile && vertCode.isNotEmpty() && fragCode.isNotEmpty()) {
            load(vertCode, fragCode)
        }
    }

    override fun vertSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + defaultPrecision + vertCode, pad)

    override fun fragSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + defaultPrecision + fragCode, pad)

    private fun getVersionStr(): String = if (APP.platformType == APP.Desktop) {
        if (profile.isNotEmpty()) {
            "#version $version $profile\n"
        } else {
            "#version $version\n"
        }
    } else {
        if (version < 330) "#version 100\n" else "#version 300 es\n"
    }

    fun load(vertexShader: String, fragmentShader: String) {
        val ver = getVersionStr()

        val fullVertCode = ver + defaultPrecision + vertexShader
        val fullFragCode = ver + defaultPrecision + fragmentShader

        compileShaders(fullVertCode, fullFragCode)
        if (isCompiled) {
            val params = IntArray(1)
            val type = IntArray(1)

            // attributes
            GL.glGetProgramiv(this.handle, GL_ACTIVE_ATTRIBUTES, params)
            val numAttributes = params[0]

            for (i in 0 until numAttributes) {
                params[0] = 1
                type[0] = 0
                val name = GL.glGetActiveAttrib(this.handle, i, params, type)
                val location = GL.glGetAttribLocation(this.handle, name)
                attributes[name] = location
            }

            // uniforms
            params[0] = 0
            GL.glGetProgramiv(this.handle, GL_ACTIVE_UNIFORMS, params)
            val numUniforms = params[0]

            for (i in 0 until numUniforms) {
                params[0] = 1
                type[0] = 0
                val name = GL.glGetActiveUniform(this.handle, i, params, type)
                val location = GL.glGetUniformLocation(this.handle, name)
                uniforms[name] = location
            }
        } else {
            if (name.isNotEmpty()) {
                LOG.info("==== Errors in shader \"$name\" ====")
            } else {
                LOG.info("==== Errors in shader ====")
            }
            LOG.info(log)
            LOG.info(sourceCode())
        }
    }

    /** Loads and compiles the shaders, creates a new program and links the shaders.
     *
     * @param vertexShader
     * @param fragmentShader
     */
    private fun compileShaders(vertexShader: String, fragmentShader: String) {
        vertexShaderHandle = loadShader(GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = loadShader(GL_FRAGMENT_SHADER, fragmentShader)

        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            isCompiledInternal = false
            return
        }

        this.handle = linkProgram(createProgram())
        if (this.handle == -1) {
            isCompiledInternal = false
            return
        }

        isCompiledInternal = true
    }

    private fun loadShader(type: Int, source: String): Int {
        val intbuf = IntArray(1)

        val shader = GL.glCreateShader(type)
        // FIXME sometimes appear an error GL_INVALID_OPERATION
        // println(GL.getErrorString(GL.glGetError()))
        if (shader == 0) return -1

        GL.glShaderSource(shader, source)
        GL.glCompileShader(shader)
        GL.glGetShaderiv(shader, GL_COMPILE_STATUS, intbuf)

        val compiled = intbuf[0]
        if (compiled == 0) {
            // gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, intbuf);
            // int infoLogLength = intbuf.get(0);
            // if (infoLogLength > 1) {
            val infoLog = GL.glGetShaderInfoLog(shader)
            logInternal += if (type == GL_VERTEX_SHADER) "Vertex shader:\n" else "Fragment shader:\n"
            logInternal += infoLog
            // }
            return -1
        }

        if (showLogs) {
            val logs = GL.glGetShaderInfoLog(shader)
            if (logs.isNotEmpty()) {
                LOG.info(if (type == GL_VERTEX_SHADER) "Vertex shader:\n" else "Fragment shader:\n")
                LOG.info(logs)
            }
        }

        return shader
    }

    protected fun createProgram(): Int {
        val program = GL.glCreateProgram()
        return if (program != 0) program else -1
    }

    private fun linkProgram(program: Int): Int {
        if (program == -1) return -1

        GL.glAttachShader(program, vertexShaderHandle)
        GL.glAttachShader(program, fragmentShaderHandle)
        GL.glLinkProgram(program)

        // https://www.khronos.org/opengl/wiki/Shader_Compilation#Cleanup
        GL.glDetachShader(program, vertexShaderHandle)
        GL.glDetachShader(program, fragmentShaderHandle)

        val tmp = IntArray(1)

        GL.glGetProgramiv(program, GL_LINK_STATUS, tmp)
        val linked = tmp[0]
        if (linked == 0) {
            // GL.glGetProgramiv(program, GL_INFO_LOG_LENGTH, intbuf);
            // int infoLogLength = intbuf.get(0);
            // if (infoLogLength > 1) {
            logInternal = GL.glGetProgramInfoLog(program)
            // }
            return -1
        }

        return program
    }

    override fun destroy() {
        isCompiledInternal = false
        logInternal = ""
        vertexShaderHandle = 0
        fragmentShaderHandle = 0
        handle = 0
        super.destroy()
    }

    private fun fetchAttributeLocation(name: String): Int {
        // -2 == not yet cached
        // -1 == cached but not found
        var location = attributes[name] ?: -2
        if (location == -2) {
            location = GL.glGetAttribLocation(this.handle, name)
            attributes[name] = location
        }
        return location
    }

    /** @param pedantic flag indicating whether attributes & uniforms must be present at all times */
    fun fetchUniformLocation(name: String, pedantic: Boolean = false): Int {
        // -1 == not found
        var location = uniforms[name]
        if (location == null) {
            location = GL.glGetUniformLocation(this.handle, name)
            require(!(location == -1 && pedantic)) { "no uniform with name '$name' in shader" }
            uniforms[name] = location
        }
        return location
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun setUniformi(name: String, value: Int) {
        GL.glUniform1i(fetchUniformLocation(name), value)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int) {
        GL.glUniform2i(fetchUniformLocation(name), value1, value2)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int, value3: Int) {
        val location = fetchUniformLocation(name)
        GL.glUniform3i(location, value1, value2, value3)
    }

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Int, value2: Int, value3: Int, value4: Int) =
        GL.glUniform4i(fetchUniformLocation(name), value1, value2, value3, value4)

    /** Sets the uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float) = GL.glUniform2f(fetchUniformLocation(name), value1, value2)

    /** Sets the vec3 uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float, value3: Float) =
        GL.glUniform3f(fetchUniformLocation(name), value1, value2, value3)

    /** Sets the vec4 uniform with the given name. Shader must be bound. */
    fun set(name: String, value1: Float, value2: Float, value3: Float, value4: Float) =
        GL.glUniform4f(fetchUniformLocation(name), value1, value2, value3, value4)

    fun set1(name: String, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform1fv(fetchUniformLocation(name), length, values, offset)

    fun set2(name: String, values: FloatArray, offset: Int, length: Int) =
        GL.glUniform2fv(fetchUniformLocation(name), length / 2, values, offset)

    fun set3(name: String, values: FloatArray, offset: Int = 0, length: Int = values.size) {
        val location = fetchUniformLocation(name)
        GL.glUniform3fv(location, length / 3, values, offset)
    }

    fun set4(name: String, values: FloatArray, offset: Int, length: Int) {
        val location = fetchUniformLocation(name)
        GL.glUniform4fv(location, length / 4, values, offset)
    }

    /** Sets the uniform matrix with the given name. Shader must be bound. */
    fun set(name: String, mat: IMat4, transpose: Boolean) {
        GL.glUniformMatrix4fv(fetchUniformLocation(name), 1, transpose, mat.values, 0)
    }

    /** Sets the uniform matrix with the given name. Shader must be bound. */
    fun set(name: String, mat: Mat3, transpose: Boolean = false) {
        GL.glUniformMatrix3fv(fetchUniformLocation(name), 1, transpose, mat.values, 0)
    }

    /** Sets an array of uniform matrices with the given name. Shader must be bound. */
    fun setUniformMatrix3fv(name: String, buffer: IFloatData, count: Int, transpose: Boolean) {
        buffer.position = 0
        val location = fetchUniformLocation(name)
        GL.glUniformMatrix3fv(location, count, transpose, buffer)
    }

    /** Sets an array of uniform matrices with the given name. Shader must be bound. */
    fun set(name: String, buffer: IFloatData, count: Int, transpose: Boolean) {
        buffer.position = 0
        val location = fetchUniformLocation(name)
        GL.glUniformMatrix4fv(location, count, transpose, buffer)
    }

    fun setMatrix4(name: String, values: FloatArray, offset: Int = 0, length: Int = values.size) =
        setMatrix4(fetchUniformLocation(name), values, offset, length)

    operator fun set(name: String, value: Boolean) = setUniformi(name, if (value) 1 else 0)

    operator fun set(name: String, value: Int) = setUniformi(name, value)

    operator fun set(name: String, value: Float) = GL.glUniform1f(fetchUniformLocation(name), value)

    /** Reduce value to float and set to uniform */
    operator fun set(name: String, value: Double) = GL.glUniform1f(fetchUniformLocation(name), value.toFloat())

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
        GL.glVertexAttrib4f(fetchAttributeLocation(name), value1, value2, value3, value4)

    fun getAttributeLocation(name: String) = attributes[name] ?: -1

    /** Some cards/drivers have different behavior */
    fun getUniformArrayLocation(name: String, errorIfNotFound: Boolean = false): Int {
        val value = uniforms[name] ?: uniforms["$name[0]"]
        if (errorIfNotFound && value == null) LOG.info("uniform \"$name\" is not found in shader \"$name\"")
        return value ?: -1
    }

    companion object {
        var showLogs = false
    }
}