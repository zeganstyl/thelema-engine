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
import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.g3d.Blending
import app.thelema.g3d.IUniformArgs
import app.thelema.gl.*
import app.thelema.shader.node.*
import app.thelema.shader.post.*
import app.thelema.utils.LOG
import app.thelema.utils.iterate
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
open class Shader(
    vertCode: String = "",
    fragCode: String = "",

    override var floatPrecision: String = "highp",
    override var version: Int = 330,
    override var profile: String = ""
): IShader {
    constructor(block: Shader.() -> Unit): this() {
        block(this)
    }

    override var vertCode: String = vertCode
    override var fragCode: String = fragCode

    /** the log  */
    protected var _log = ""
    override val log
        get() = if (isCompiled) GL.glGetProgramInfoLog(this.programHandle) else _log

    protected var _isCompiled: Boolean = false
    override val isCompiled: Boolean
        get() = _isCompiled

    override val uniforms = HashMap<String, Int>()

    var vertexShaderHandle: Int = 0
    var fragmentShaderHandle: Int = 0
    var programHandle: Int = 0

    override var nodes: Array<IShaderNode> = emptyArray()

    override var listener: ShaderListener? = null

    override var depthMask: Boolean = true

    override val componentName: String
        get() = "Shader"

    override var entityOrNull: IEntity? = null

    override var rootNode: IRootShaderNode? = null
        set(value) {
            if (field != value) {
                field = value
                buildRequested = true
            }
        }

    override var buildRequested: Boolean = true

    override var vertexLayout: IVertexLayout = Vertex.Layout
        set(value) {
            if (field != value) {
                field = value
                buildRequested = true
            }
        }

    override var uniformArgs: IUniformArgs? = null

//    init {
//        if (compile && vertCode.isNotEmpty() && fragCode.isNotEmpty()) {
//            load(vertCode, fragCode)
//        }
//    }

    val uniformBuffersMap = HashMap<String, IUniformBuffer>(1)
    val uniformBuffers = ArrayList<IUniformBuffer>(1)

    override fun getOrCreateUniformBuffer(layout: IUniformLayout): IUniformBuffer {
        var b = uniformBuffersMap[layout.name]
        if (b == null) {
            b = when (layout.name) {
                MeshUniforms.name -> MeshUniformBuffer()
                else -> UniformBuffer(layout)
            }
            uniformBuffersMap[layout.name] = b
            uniformBuffers.add(b)
        }
        return b
    }

    override fun bindUniformBuffer(buffer: IUniformBuffer) {
        if (buffer.gpuUploadRequested) {
            buffer.bytes.rewind()
            buffer.uploadBufferToGpu()
        }

        val index = GL.glGetUniformBlockIndex(programHandle, buffer.layout.name)
        GL.glUniformBlockBinding(programHandle, index, buffer.layout.bindingPoint)
        GL.glBindBufferBase(GL_UNIFORM_BUFFER, buffer.layout.bindingPoint, buffer.bufferHandle)
    }

    override fun bindOwnUniformBuffers() {
        uniformBuffers.iterate { bindUniformBuffer(it) }
    }

    override fun get(uniformName: String): Int = GL.glGetUniformLocation(programHandle, uniformName)

    override fun getUniformLocation(name: String, pedantic: Boolean): Int {
        // -1 == not found
        var location = uniforms[name]
        if (location == null) {
            location = GL.glGetUniformLocation(this.programHandle, name)
            require(!(location == -1 && pedantic)) { "no uniform with name '$name' in shader" }
            uniforms[name] = location
        }
        return location
    }

    /** Every line will be numerated */
    fun fragFullCode(): String {
        val ver = getVersionStr()
        val floatPrecision = getFloatPrecisionStr()

        var vert = if (version >= 330 && refineShaderCode) refineFragmentCode(fragCode) else fragCode

        vert = "#uniforms\\s+\\w+".toRegex().replace(vert) {
            val str = it.value.trimEnd()
            var i = str.length
            val builder = StringBuilder(str.length)
            while (i > 0) {
                i--
                val c = str[i]
                if (c.isWhitespace()) break
                builder.append(c)
            }
            builder.reverse()
            val name = builder.toString()
            val ub = UniformLayouts[name]
            if (ub == null) {
                throw IllegalStateException("Uniform block is not found: $name")
            } else {
                builder.clear()
                ub.layoutDefinition(builder)
                builder.toString()
            }
        }

        return ver + floatPrecision + vert
    }

    override fun printCode(pad: Int): String {
        return numerateLines("=== VERTEX ===\n", vertFullCode(), pad) +
                numerateLines("=== FRAGMENT ===\n", fragFullCode(), pad)
    }

    fun vertFullCode(): String {
        val ver = getVersionStr()
        val floatPrecision = getFloatPrecisionStr()

        var vert = if (version >= 330 && refineShaderCode) refineVertexCode(vertCode) else vertCode

        vert = "#uniforms\\s+\\w+".toRegex().replace(vert) {
            val str = it.value.trimEnd()
            var i = str.length
            val builder = StringBuilder(str.length)
            while (i > 0) {
                i--
                val c = str[i]
                if (c.isWhitespace()) break
                builder.append(c)
            }
            builder.reverse()
            val name = builder.toString()
            val ub = UniformLayouts[name]
            if (ub == null) {
                throw IllegalStateException("Uniform block is not found: $name")
            } else {
                builder.clear()
                ub.layoutDefinition(builder)
                builder.toString()
            }
        }

        return ver + floatPrecision + vert
    }

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

    private fun refineAttributes(vert: String): String {
        return "in\\s+\\w+\\s+\\w+\\s*;".toRegex().replace(vert) {
            val str = it.value.substringBefore(';').trimEnd()
            var i = str.length
            val builder = StringBuilder(str.length)
            while (i > 0) {
                i--
                val c = str[i]
                if (c.isWhitespace()) break
                builder.append(c)
            }
            builder.reverse()
            val name = builder.toString()
            val a = vertexLayout.getAttributeByNameOrNull(name)
            if (a == null) {
                it.value
            } else {
                "layout (location = ${a.id}) in ${a.getGlslType()} ${a.name};"
            }
        }
    }

    private fun refineVertexCode(vertCode: String): String {
        val vert = vertCode
            .replace("\nattribute ", "\nin ")
            .replace("\nvarying ", "\nout ")

        return refineAttributes(vert)
    }

    private fun refineFragmentCode(fragCode: String): String {
        var frag = fragCode
            .replace("\nvarying ", "\nin ")
            .replace("texture2D(", "texture(")
            .replace("textureCube(", "texture(")

        if (frag.contains("gl_FragColor")) {
            frag = frag.replace("gl_FragColor", "FragColor")
            frag = "out vec4 FragColor;\n$frag"
        }

        return frag
    }

    override fun load(vertCode: String, fragCode: String) {
        compileShaders(vertFullCode(), fragFullCode())
        if (isCompiled) {
            val params = IntArray(1)
            val type = IntArray(1)

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
            if (showCodeOnError) {
                LOG.info(printCode())
            } else {
                LOG.info("You can set \"Shader.showCodeOnError = true\" to see shader code")
            }
        }

        listener?.loaded(this)
    }

    /** Loads and compiles the shaders, creates a new program and links the shaders.
     *
     * @param vertexShader
     * @param fragmentShader
     */
    private fun compileShaders(vertexShader: String, fragmentShader: String) {
        _log = ""
        vertexShaderHandle = loadShader(GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = loadShader(GL_FRAGMENT_SHADER, fragmentShader)

        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            _isCompiled = false
            return
        }

        this.programHandle = linkProgram(GL.glCreateProgram())
        if (this.programHandle == -1) {
            _isCompiled = false
            return
        }

        _isCompiled = true
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
            _log += if (type == GL_VERTEX_SHADER) "Vertex shader:\n" else "Fragment shader:\n"
            _log += infoLog
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
            _log += GL.glGetProgramInfoLog(program)
            // }
            return -1
        }

        return program
    }

    private fun collectNodes(root: IShaderNode, out: MutableSet<IShaderNode>) {
        if (out.add(root)) {
            root.inputs.iterate { input ->
                input.valueOrDefault()?.container?.also {
                    collectNodes(it, out)
                }
            }
        }
    }


    private fun findNodeRecursive(root: IShaderNode, componentName: String): IShaderNode? {
        if (root.componentName == componentName) return root
        root.inputs.iterate { input ->
            input.value?.container?.also {
                findNodeRecursive(it, componentName)?.also { return it }
            }
        }
        return null
    }

    override fun findShaderNode(componentName: String): IShaderNode? {
        val rootNode = rootNode?.proxy ?: return null
        return findNodeRecursive(rootNode, componentName)
    }

    protected open fun buildByNodes() {
        val rootNode = rootNode?.proxy ?: return

        // collect all connected nodes to know about full tree
        val nodeSet = HashSet<IShaderNode>()
        collectNodes(rootNode, nodeSet)
        nodes = nodeSet.toTypedArray()
        nodes.iterate { node ->
            node.shaderOrNull = this
            node.forEachOutput { it.isUsed = false }
        }

        nodes.iterate { node ->
            node.inputs.iterate { it.valueOrDefault()?.isUsed = true }
        }

        nodes.iterate { it.prepareToBuild() }

        sortNodes(nodes)

        val vertDecl = StringBuilder()
        val vertExe = StringBuilder()
        val fragDecl = StringBuilder()
        val fragExe = StringBuilder()

        vertDecl.append("out vec4 $WORLD_SPACE_POS;\n")
        vertDecl.append("out vec4 $CLIP_SPACE_POS;\n")
        fragDecl.append("in vec4 $WORLD_SPACE_POS;\n")
        fragDecl.append("in vec4 $CLIP_SPACE_POS;\n")

        SceneUniforms.layoutDefinition(vertDecl)
        MeshUniforms.layoutDefinition(vertDecl)
        SceneUniforms.layoutDefinition(fragDecl)
        MeshUniforms.layoutDefinition(fragDecl)

        nodes.iterate {
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
        buildRequested = false
        if (rootNode != null) buildByNodes()

        if (isCompiled) destroy()
        load(vertCode, fragCode)
        if (!isCompiled) {
            LOG.error("Errors in generated shader")
        }
        GL.glUseProgram(this.programHandle)
        nodes.iterate { it.shaderCompiled() }
    }

    override fun bind() {
        if (buildRequested) build()
        GL.glUseProgram(this.programHandle)

        GL.isDepthMaskEnabled = depthMask

        GL.resetTextureUnitCounter()

        for (i in nodes.indices) {
            val node = nodes[i]
            node.shaderOrNull = this
            uniformArgs?.also { node.bind(it) }
        }

        listener?.bind(this)
    }

    override fun requestBuild() {
        buildRequested = true
    }

    override fun destroy() {
        super.destroy()

        GL.glUseProgram(0)
        GL.glDeleteShader(vertexShaderHandle)
        GL.glDeleteShader(fragmentShaderHandle)
        GL.glDeleteProgram(this.programHandle)

        uniforms.clear()

        _isCompiled = false
        _log = ""
        vertexShaderHandle = 0
        fragmentShaderHandle = 0
        programHandle = 0
    }

    @ThreadLocal
    companion object {
        var refineShaderCode: Boolean = true
        var showCodeOnError: Boolean = true

        const val CLIP_SPACE_POS = "CLIP_SPACE_POS"
        const val WORLD_SPACE_POS = "WORLD_SPACE_POS"

        /** Sort nodes with order:
         * less depth - node will be first,
         * more depth - node will be last */
        fun sortNodes(out: Array<IShaderNode>) {
            val countMap = HashMap<IShaderNode, Int>()
            out.iterate { countMap[it] = findMaxChildrenTreeDepth(it) }
            out.sortBy { countMap[it] }
        }

        fun findMaxChildrenTreeDepth(root: IShaderNode, count: Int = 0): Int {
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

        fun setupShaderComponents() {
            ECS.descriptor({ Shader() }) {
                setAliases(IShader::class)

                ref(Shader::rootNode)
                int(Shader::version)
                string(Shader::profile)
                string(Shader::floatPrecision)
                bool(Shader::depthMask)

                descriptor({ FXAA() }) {
                    float(FXAA::lumaThreshold)
                    float(FXAA::reduceMin)
                    float(FXAA::reduceMul)
                    float(FXAA::spanMax)
                }

                descriptor({ Bloom(APP.width, APP.height) }) {
                    float(Bloom::intensity)
                }

                descriptor({ GodRays() }) {

                }

                descriptor({ MotionBlur() }) {
                    float(MotionBlur::blurStrength)
                }

                descriptor({ Threshold() }) {
                    float(Threshold::cutoff)
                    vec3(Threshold::thresholdColor)
                }

                descriptor({ Vignette() }) {
                    float(Vignette::radius)
                    float(Vignette::softness)
                }

                descriptor({ VertexNode() }) {
                    string(VertexNode::worldTransformType)
                    shaderNodeOutput(VertexNode::position)
                    shaderNodeOutput(VertexNode::normal)
                    shaderNodeOutput(VertexNode::tbn)
                }

                descriptor({ UVNode() }) {
                    string(UVNode::uvName)
                    shaderNodeOutput(UVNode::uv)
                }

                descriptor({ Texture2DNode() }) {
                    refAbs(Texture2DNode::texture)
                    bool(Texture2DNode::sRGB)
                    shaderNodeInput(Texture2DNode::uv)
                    shaderNodeOutput(Texture2DNode::texColor)
                    shaderNodeOutput(Texture2DNode::texAlpha)
                }

                descriptor({ TextureCubeNode() }) {
                    refAbs(TextureCubeNode::texture)
                    bool(TextureCubeNode::sRGB)
                    shaderNodeInput(TextureCubeNode::uv)
                    shaderNodeOutput(TextureCubeNode::texColor)
                    shaderNodeOutput(TextureCubeNode::texAlpha)
                }

                descriptor({ Texture3DNode() }) {
                    refAbs(Texture3DNode::texture)
                    bool(Texture3DNode::sRGB)
                    shaderNodeInput(Texture3DNode::uv)
                    shaderNodeOutput(Texture3DNode::texColor)
                    shaderNodeOutput(Texture3DNode::texAlpha)
                }

                descriptor({ UniformFloatNode() }) {
                    string(UniformFloatNode::uniformName)
                    float(UniformFloatNode::defaultValue)
                    shaderNodeOutput(UniformFloatNode::uniform)
                }
                descriptor({ UniformVec2Node() }) {
                    string(UniformVec2Node::uniformName)
                    vec2(UniformVec2Node::defaultValue)
                    shaderNodeOutput(UniformVec2Node::uniform)
                }
                descriptor({ UniformVec3Node() }) {
                    string(UniformVec3Node::uniformName)
                    vec3(UniformVec3Node::defaultValue)
                    shaderNodeOutput(UniformVec3Node::uniform)
                }
                descriptor({ UniformVec4Node() }) {
                    string(UniformVec4Node::uniformName)
                    vec4(UniformVec4Node::defaultValue)
                    shaderNodeOutput(UniformVec4Node::uniform)
                }
                descriptor({ UniformIntNode() }) {
                    string(UniformIntNode::uniformName)
                    int(UniformIntNode::defaultValue)
                    shaderNodeOutput(UniformIntNode::uniform)
                }

                descriptor({ NormalMapNode() }) {
                    shaderNodeInput(NormalMapNode::tbn)
                    shaderNodeInput(NormalMapNode::color)
                    shaderNodeInput(NormalMapNode::scale)
                    shaderNodeOutput(NormalMapNode::normal)
                }

                descriptor({ NormalToTBNNode() }) {
                    shaderNodeInput(NormalToTBNNode::worldPosition)
                    shaderNodeInput(NormalToTBNNode::normal)
                    shaderNodeInput(NormalToTBNNode::uv)
                    shaderNodeOutput(NormalToTBNNode::tbn)
                }

                descriptor({ PBRNode() }) {
                    bool(PBRNode::iblEnabled)
                    bool(PBRNode::receiveShadows)
                    int(PBRNode::shadowCascadesNum)
                    shaderNodeInput(PBRNode::baseColor)
                    shaderNodeInputOrNull(PBRNode::alpha)
                    shaderNodeInput(PBRNode::normal)
                    shaderNodeInputOrNull(PBRNode::occlusion)
                    shaderNodeInputOrNull(PBRNode::metallic)
                    shaderNodeInputOrNull(PBRNode::roughness)
                    shaderNodeInputOrNull(PBRNode::emissive)
                    shaderNodeInput(PBRNode::worldPosition)
                    shaderNodeOutput(PBRNode::result)
                }

                descriptor({ OutputNode() }) {
                    shaderNodeInput(OutputNode::vertPosition)
                    shaderNodeInput(OutputNode::fragColor)

                    stringEnum(OutputNode::alphaMode, Blending.items)
                    float(OutputNode::alphaCutoff, 0.5f)
                    float(OutputNode::fadeStart, 0.8f)

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

                descriptor({ RootShaderNode() }) {
                    setAliases(IRootShaderNode::class)
                }

                descriptor({ ParticleDataNode() }) {
                    shaderNodeOutput(ParticleDataNode::lifeTimePercent)
                }

                descriptor({ ParticlePositionNode() }) {
                    bool(ParticlePositionNode::alwaysRotateToCamera)
                    shaderNodeInput(ParticlePositionNode::vertexPosition)
                    shaderNodeOutput(ParticlePositionNode::particlePosition)
                }

                descriptor({ ParticleScaleNode() }) {
                    float(ParticleScaleNode::scaling)
                    shaderNodeInput(ParticleScaleNode::lifeTimePercent)
                    shaderNodeInput(ParticleScaleNode::inputPosition)
                    shaderNodeOutput(ParticleScaleNode::scaledPosition)
                }

                descriptor({ ParticleUVFrameNode() }) {
                    int(ParticleUVFrameNode::framesU, 1)
                    int(ParticleUVFrameNode::framesV, 1)
                    shaderNodeInput(ParticleUVFrameNode::inputUv)
                    shaderNodeOutput(ParticleUVFrameNode::resultUv)
                }

                descriptor({ ParticleColorNode() }) {
                    shaderNodeInput(ParticleColorNode::color0)
                    shaderNodeInput(ParticleColorNode::color1)
                    shaderNodeInput(ParticleColorNode::inputColor)
                    shaderNodeInput(ParticleColorNode::lifeTimePercent)
                    shaderNodeOutput(ParticleColorNode::result)
                }

                descriptor({ SplitVec4Node() }) {

                }

                descriptor({ MergeVec4() }) {

                }

                descriptor({ AttributeNode() }) {

                }

                descriptor({ GBufferOutputNode() }) {

                }

                descriptor({ GBufferOutputNode() }) {

                }

                descriptor({ Op2Node() }) {
                    string(Op2Node::function)
                    string(Op2Node::resultType)
                    bool(Op2Node::isFragment, true)
                    bool(Op2Node::isVarying, true)
                    shaderNodeInput(Op2Node::in1)
                    shaderNodeInput(Op2Node::in2)
                    shaderNodeOutput(Op2Node::opResult)
                }

                descriptor({ TerrainTileNode() }) {
                    float(TerrainTileNode::uvScale, 0.1f)
                    shaderNodeInput(TerrainTileNode::inputPosition)
                    shaderNodeOutput(TerrainTileNode::tilePositionScale)
                    shaderNodeOutput(TerrainTileNode::outputPosition)
                    shaderNodeOutput(TerrainTileNode::uv)
                }

                descriptor({ HeightMapNode() }) {
                    refAbs(HeightMapNode::texture)
                    vec3(HeightMapNode::mapWorldSize)
                    float(HeightMapNode::uvScale)
                    shaderNodeInput(HeightMapNode::inputPosition)
                    shaderNodeOutput(HeightMapNode::height)
                    shaderNodeOutput(HeightMapNode::texColor)
                    shaderNodeOutput(HeightMapNode::outputPosition)
                }

                descriptor({ ToneMapNode() }) {
                    shaderNodeInput(ToneMapNode::inputColor)
                    shaderNodeOutput(ToneMapNode::result)
                }

                descriptor({ VelocityOutputNode() }) {

                }

                descriptor({ GLSLFloatLiteral() }) {
                    float(GLSLFloatLiteral::value)
                }
                descriptor({ GLSLVec2Literal() }) {
                    vec2(GLSLVec2Literal::value)
                }
                descriptor({ GLSLVec3Literal() }) {
                    vec3(GLSLVec3Literal::value)
                }
                descriptor({ GLSLVec4Literal() }) {
                    vec4(GLSLVec4Literal::value)
                }
            }
        }
    }
}