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

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.g3d.IScene
import app.thelema.gl.*
import app.thelema.shader.node.*
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/** @author zeganstyl */
open class Shader(
    vertCode: String = "",
    fragCode: String = "",
    compile: Boolean = true,

    /** Precision will be added as first line */
    override var floatPrecision: String = "mediump",
    override var version: Int = 110,
    override var profile: String = ""
): IShader {
    constructor(block: Shader.() -> Unit): this() {
        block(this)
    }

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

    override val nodes: MutableList<IShaderNode> = ArrayList(1)

    var actualNodes: Array<IShaderNode> = emptyArray()

    override var onPrepareShader: IShader.(mesh: IMesh, scene: IScene?) -> Unit = { _, _ -> }

    override var depthMask: Boolean = true

    override val componentName: String
        get() = "Shader"

    override var entityOrNull: IEntity? = null

    init {
        if (compile && vertCode.isNotEmpty() && fragCode.isNotEmpty()) {
            load(vertCode, fragCode)
        }
    }

    override fun vertSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + getFloatPrecisionStr() + vertCode, pad)

    override fun fragSourceCode(title: String, pad: Int): String =
        numerateLines(title, getVersionStr() + getFloatPrecisionStr() + fragCode, pad)

    private fun getFloatPrecisionStr(): String = if (GL.isGLES) "precision $floatPrecision float;\n" else ""

    private fun getVersionStr(): String = if (GL.isGLES) {
        if (version < 330) "#version 100\n" else "#version 300 es\n"
    } else {
        if (profile.isNotEmpty()) {
            "#version $version $profile\n"
        } else {
            "#version $version\n"
        }
    }

    override fun load(vertCode: String, fragCode: String) {
        val ver = getVersionStr()
        val floatPrecision = getFloatPrecisionStr()

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
            val path = entityOrNull?.path ?: ""
            if (path.isNotEmpty()) {
                LOG.error("==== Errors in shader \"$path\" ====")
            } else {
                LOG.error("==== Errors in shader ====")
            }
            if (log.isNotBlank()) {
                LOG.info("=== INFO LOG ===")
                LOG.info(log)
                LOG.info("=== INFO LOG END ===")
            }
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

    override fun prepareShader(mesh: IMesh, scene: IScene?) {
        GL.resetTextureUnitCounter()
        bind()

        onPrepareShader(mesh, scene)

        for (i in actualNodes.indices) {
            val node = actualNodes[i]
            node.shaderOrNull = this
            node.prepareShaderNode(mesh, scene)
        }
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

    private fun findMaxChildrenTreeDepth(root: IShaderNode, count: Int = 0): Int {
        if (root.inputs.isNotEmpty()) {
            val incCount = count + 1
            var maxCount = incCount
            root.inputs.iterate {
                val container = it.value?.container
                if (container != root && container != null) {
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

    private fun collectNodes(root: IShaderNode, out: MutableSet<IShaderNode>) {
        if (out.add(root)) {
            root.inputs.iterate { input ->
                input.value?.container?.also {
                    collectNodes(it, out)
                }
            }
        }
    }

    override fun buildByNodes() {
        // collect all connected nodes to know about full tree
        val nodeSet = HashSet<IShaderNode>()
        nodes.iterate { collectNodes(it, nodeSet) }
        actualNodes = nodeSet.toTypedArray()
        actualNodes.iterate { node ->
            node.shaderOrNull = this
        }

        actualNodes.forEach { it.prepareToBuild() }

        // Sort nodes with order: less dependencies depth - first, more dependencies depth - last
        val countMap = HashMap<IShaderNode, Int>()
        actualNodes.forEach { countMap[it] = findMaxChildrenTreeDepth(it) }
        actualNodes.sortBy { countMap[it] }
        nodes.sortBy { countMap[it] }

        val vertDecl = StringBuilder()
        val vertExe = StringBuilder()
        val fragDecl = StringBuilder()
        val fragExe = StringBuilder()

        actualNodes.forEach {
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
            LOG.error("Errors in generated shader")
        }
        bind()
        actualNodes.forEach { it.shaderCompiled() }
    }

    override fun destroy() {
        isCompiledInternal = false
        logInternal = ""
        vertexShaderHandle = 0
        fragmentShaderHandle = 0
        programHandle = 0
        super.destroy()
    }

    companion object {
        fun setupShaderComponents() {
            ECS.descriptor({ Shader() }) {
                setAliases(IShader::class)

                int(Shader::version)
                string(Shader::profile)
                string(Shader::floatPrecision)
                bool(Shader::depthMask)

                descriptor({ VertexNode() }) {
                    int(VertexNode::maxBones)
                    int(VertexNode::bonesSetsNum)
                    string(VertexNode::positionName)
                    string(VertexNode::normalName)
                    string(VertexNode::tangentName)
                    string(VertexNode::worldTransformType)
                    string(VertexNode::bonesName)
                    string(VertexNode::boneWeightsName)
                }

                descriptor({ CameraDataNode() }) {
                    string(CameraDataNode::instancePositionName)
                    bool(CameraDataNode::alwaysRotateObjectToCamera)
                    bool(CameraDataNode::useInstancePosition)
                }

                descriptor({ UVNode() }) {
                    string(UVNode::uvName)
                }

                descriptor({ TextureNode() }) {
                    refAbs(TextureNode::texture)
                    bool(TextureNode::sRGB)
                    stringEnum(
                        TextureNode::textureType,
                        listOf(GLSLType.Sampler1D, GLSLType.Sampler2D, GLSLType.Sampler2DArray, GLSLType.SamplerCube, GLSLType.Sampler3D)
                    )
                }

                descriptor({ NormalMapNode() }) {

                }

                descriptor({ PBRNode() }) {
                    bool(PBRNode::iblEnabled)
                    bool(PBRNode::receiveShadows)
                    int(PBRNode::shadowCascadesNum)
                }

                descriptor({ OutputNode() }) {
                    intEnum(
                        OutputNode::cullFaceMode,
                        mapOf(
                            0 to "Disabled",
                            GL_BACK to "Back",
                            GL_FRONT to "Front",
                            GL_FRONT_AND_BACK to "Front and Back"
                        ),
                        "Disabled"
                    )
                }

                descriptor({ ParticleDataNode() }) {

                }

                descriptor({ ParticleScaleNode() }) {
                    float(ParticleScaleNode::scaling)
                }

                descriptor({ ParticleUVFrameNode() }) {
                    float(ParticleUVFrameNode::frameSizeU)
                    float(ParticleUVFrameNode::frameSizeV)
                    string(ParticleUVFrameNode::instanceUvStartName)
                }

                descriptor({ ParticleColorNode() }) {

                }
            }
        }
    }
}