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

import app.thelema.app.APP
import app.thelema.gl.*
import app.thelema.shader.node.IShaderData
import app.thelema.shader.node.IShaderNode
import app.thelema.utils.LOG

/** @author zeganstyl */
open class Shader(
    vertCode: String = "",
    fragCode: String = "",
    override var name: String = "",
    compile: Boolean = true,

    /** Precision will be added as first line */
    var floatPrecision: String = if (APP.platformType != APP.Desktop) "precision mediump float;\n" else "",
    override var version: Int = 110,
    override var profile: String = ""
): IShader {
    override var vertCode: String = vertCode
    override var fragCode: String = fragCode

    /** the log  */
    protected var logInternal = ""
    override val log
        get() = if (isCompiled) GL.glGetProgramInfoLog(this.programHandle) else logInternal

    protected var isCompiledInternal: Boolean = false
    override val isCompiled: Boolean
        get() = isCompiledInternal

    override val uniforms = HashMap<String, Int>()
    override val attributes = HashMap<String, Int>()

    protected val enabledAttributesInternal = ArrayList<IVertexAttribute>()
    override val enabledAttributes: List<IVertexAttribute>
        get() = enabledAttributesInternal

    override var vertexShaderHandle: Int = 0
    override var fragmentShaderHandle: Int = 0
    override var programHandle: Int = 0

    override val nodes: MutableList<IShaderNode> = ArrayList()

    protected val uids = HashMap<IShaderData, String>()

    override var onMeshDraw: IShader.(mesh: IMesh) -> Unit = {}

    init {
        if (compile && vertCode.isNotEmpty() && fragCode.isNotEmpty()) {
            load(vertCode, fragCode)
        }
    }

    override fun getUID(data: IShaderData): String = uids[data] ?: ""

    override fun vertSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + floatPrecision + vertCode, pad)

    override fun fragSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + floatPrecision + fragCode, pad)

    private fun getVersionStr(): String = if (APP.platformType == APP.Desktop) {
        if (profile.isNotEmpty()) {
            "#version $version $profile\n"
        } else {
            "#version $version\n"
        }
    } else {
        if (version < 330) "#version 100\n" else "#version 300 es\n"
    }

    fun load(vertCode: String, fragCode: String) {
        val ver = getVersionStr()

        val fullVertCode = ver + floatPrecision + vertCode
        val fullFragCode = ver + floatPrecision + fragCode

        compileShaders(fullVertCode, fullFragCode)
        if (isCompiled) {
            val params = IntArray(1)
            val type = IntArray(1)

            // attributes
            GL.glGetProgramiv(this.programHandle, GL_ACTIVE_ATTRIBUTES, params)
            val numAttributes = params[0]

            for (i in 0 until numAttributes) {
                params[0] = 1
                type[0] = 0
                val name = GL.glGetActiveAttrib(this.programHandle, i, params, type)
                val location = GL.glGetAttribLocation(this.programHandle, name)
                attributes[name] = location
            }

            // uniforms
            params[0] = 0
            GL.glGetProgramiv(this.programHandle, GL_ACTIVE_UNIFORMS, params)
            val numUniforms = params[0]

            for (i in 0 until numUniforms) {
                params[0] = 1
                type[0] = 0
                val name = GL.glGetActiveUniform(this.programHandle, i, params, type)
                val location = GL.glGetUniformLocation(this.programHandle, name)
                uniforms[name] = location
            }
        } else {
            LOG.error(GL.getErrorString())
            if (name.isNotEmpty()) {
                LOG.error("==== Errors in shader \"$name\" ====")
            } else {
                LOG.error("==== Errors in shader ====")
            }
            LOG.info(log)
            LOG.info(printCode())
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

        this.programHandle = linkProgram(GL.glCreateProgram())
        if (this.programHandle == -1) {
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

        return shader
    }

    private fun linkProgram(program: Int): Int {
        if (program == 0) return -1

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

    override fun disableAllAttributes() {
        attributes.values.forEach {
            GL.glDisableVertexAttribArray(it)
        }
        enabledAttributesInternal.clear()
    }

    override fun disableAttribute(attribute: IVertexAttribute) {
        val location = attributes[attribute.name]
        if (location != null) {
            GL.glDisableVertexAttribArray(location)
            enabledAttributesInternal.remove(attribute)
        }
    }

    override fun enableAttribute(attribute: IVertexAttribute) {
        val location = attributes[attribute.name]
        if (location != null) {
            GL.glEnableVertexAttribArray(location)
            GL.glVertexAttribPointer(
                location,
                attribute.size,
                attribute.type,
                attribute.normalized,
                attribute.stride,
                attribute.byteOffset
            )

            // TODO GLFeature
            if ((GL.isGLES && GL.glesMajVer >= 3) || !GL.isGLES) {
                GL.glVertexAttribDivisor(location, attribute.divisor)
            }

            enabledAttributesInternal.add(attribute)
        }
    }

    override fun buildByNodes() {
        uids.clear()
        var uid = 0
        for (i in nodes.indices) {
            val node = nodes[i]
            node.shaderOrNull = this
            node.output.forEach {
                uids[it.value] = uid.toString()
                uid++
            }
        }

        nodes.forEach { it.prepareToBuild() }

        // Sort nodes with order: less dependencies depth - first, more dependencies depth - last
        val countMap = HashMap<IShaderNode, Int>()
        nodes.forEach { countMap[it] = findMaxChildrenTreeDepth(it) }
        nodes.sortBy { countMap[it] }

        val vertDecl = StringBuilder()
        val vertExe = StringBuilder()
        val fragDecl = StringBuilder()
        val fragExe = StringBuilder()

        nodes.forEach {
            it.declarationVert(vertDecl)
            it.executionVert(vertExe)
            it.declarationFrag(fragDecl)
            it.executionFrag(fragExe)
        }

        vertDecl.append('\n')
        vertDecl.append("void main() {\n")
        vertDecl.append(vertExe)
        vertDecl.append('\n')
        vertDecl.append("}\n")
        vertCode = vertDecl.toString()

        fragDecl.append('\n')
        fragDecl.append("void main() {\n")
        fragDecl.append(fragExe)
        fragDecl.append('\n')
        fragDecl.append("}\n")
        fragCode = fragDecl.toString()
    }

    override fun build() {
        buildByNodes()
        if (isCompiled) destroy()
        load(vertCode, fragCode)
        if (!isCompiled) {
            LOG.info(printCode())
            throw RuntimeException("Errors in generated shader")
        }
        bind()
        nodes.forEach { it.shaderCompiled() }
    }

    private fun findMaxChildrenTreeDepth(node: IShaderNode, count: Int = 0): Int {
        if (node.input.isNotEmpty()) {
            val incCount = count + 1
            var maxCount = incCount
            node.input.values.forEach {
                val container = it.container
                if (container != node && container != null) {
                    val childCount = findMaxChildrenTreeDepth(container, incCount)
                    if (childCount > maxCount) {
                        maxCount = childCount
                    }
                }
            }
            return maxCount
        }
        return count
    }

    override fun destroy() {
        isCompiledInternal = false
        logInternal = ""
        vertexShaderHandle = 0
        fragmentShaderHandle = 0
        programHandle = 0
        super.destroy()
    }
}