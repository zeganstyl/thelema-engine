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
import app.thelema.g3d.IScene
import app.thelema.gl.*
import app.thelema.shader.node.*
import app.thelema.shader.post.*
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/** @author zeganstyl */
open class Shader(
    vertCode: String = "",
    fragCode: String = "",
    compile: Boolean = true,

    /** Precision will be added as first line */
    override var floatPrecision: String = "highp",
    override var version: Int = if (GL.glesMajVer < 3) 110 else 330,
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
    override val attributes = HashMap<String, Int>()

    protected val enabledAttributesInternal = ArrayList<IVertexAttribute>()
    override val enabledAttributes: List<IVertexAttribute>
        get() = enabledAttributesInternal

    override var vertexShaderHandle: Int = 0
    override var fragmentShaderHandle: Int = 0
    override var programHandle: Int = 0

    override var nodes: Array<IShaderNode> = emptyArray()

    override var onPrepareShader: IShader.(mesh: IMesh, scene: IScene?) -> Unit = { _, _ -> }

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

    override var buildRequested: Boolean = false

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

        var vert = vertCode
        var frag = fragCode
        if (version >= 330 && refineShaderCode) {
            vert = vertCode
                .replace("\nattribute ", "\nin ")
                .replace("\nvarying ", "\nout ")

            frag = fragCode
                .replace("\nvarying ", "\nin ")
                .replace("texture2D(", "texture(")

            if (frag.contains("gl_FragColor")) {
                frag = "out vec4 gl_FragColor;\n$frag"
            }
        }
        this.vertCode = vert
        this.fragCode = frag

        val fullVertCode = ver + floatPrecision + vert
        val fullFragCode = ver + floatPrecision + frag

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

    override fun prepareShader(mesh: IMesh, scene: IScene?) {
        GL.resetTextureUnitCounter()
        bind()

        onPrepareShader(mesh, scene)

        for (i in nodes.indices) {
            val node = nodes[i]
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
        bind()
        nodes.iterate { it.shaderCompiled() }
    }

    override fun requestBuild() {
        buildRequested = true
    }

    override fun destroy() {
        _isCompiled = false
        _log = ""
        vertexShaderHandle = 0
        fragmentShaderHandle = 0
        programHandle = 0
        super.destroy()
    }

    companion object {
        var refineShaderCode: Boolean = true

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
                    int(VertexNode::maxBones)
                    int(VertexNode::bonesSetsNum)
                    string(VertexNode::positionName)
                    string(VertexNode::normalName)
                    string(VertexNode::tangentName)
                    string(VertexNode::worldTransformType)
                    string(VertexNode::bonesName)
                    string(VertexNode::boneWeightsName)
                    shaderNodeOutput(VertexNode::position)
                    shaderNodeOutput(VertexNode::normal)
                    shaderNodeOutput(VertexNode::tbn)
                }

                descriptor({ CameraDataNode() }) {
                    string(CameraDataNode::instancePositionName)
                    bool(CameraDataNode::alwaysRotateObjectToCamera)
                    bool(CameraDataNode::useInstancePosition)
                    shaderNodeInput(CameraDataNode::vertexPosition)
                    shaderNodeOutput(CameraDataNode::clipSpacePosition)
                    shaderNodeOutput(CameraDataNode::cameraPosition)
                    shaderNodeOutput(CameraDataNode::normalizedViewVector)
                    shaderNodeOutput(CameraDataNode::viewZDepth)
                    shaderNodeOutput(CameraDataNode::inverseViewProjectionMatrix)
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
                    shaderNodeInput(NormalMapNode::vertexPosition)
                    shaderNodeInput(NormalMapNode::normalizedViewVector)
                    shaderNodeInput(NormalMapNode::tbn)
                    shaderNodeInput(NormalMapNode::uv)
                    shaderNodeInput(NormalMapNode::normalColor)
                    shaderNodeInput(NormalMapNode::normalScale)
                    shaderNodeOutput(NormalMapNode::tangent)
                    shaderNodeOutput(NormalMapNode::biNormal)
                    shaderNodeOutput(NormalMapNode::normal)
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
                    shaderNodeInput(PBRNode::normalizedViewVector)
                    shaderNodeInput(PBRNode::worldPosition)
                    shaderNodeInput(PBRNode::clipSpacePosition)
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
                    string(ParticleDataNode::instanceLifeTimeName, "INSTANCE_LIFE_TIME")
                    float(ParticleDataNode::maxLifeTime, 1f)
                    shaderNodeInput(ParticleDataNode::inputUv)
                    shaderNodeOutput(ParticleDataNode::lifeTime)
                    shaderNodeOutput(ParticleDataNode::lifeTimePercent)
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
                    string(ParticleUVFrameNode::instanceUvStartName, "INSTANCE_UV_START")
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

                descriptor({ TerrainVertexNode() }) {

                }

                descriptor({ HeightMapNode() }) {

                }

                descriptor({ SkyboxVertexNode() }) {
                    string(SkyboxVertexNode::positionsName, "POSITION")
                    shaderNodeOutput(SkyboxVertexNode::clipSpacePosition)
                    shaderNodeOutput(SkyboxVertexNode::worldSpacePosition)
                    shaderNodeOutput(SkyboxVertexNode::textureCoordinates)
                    shaderNodeOutput(SkyboxVertexNode::velocity)
                }

                descriptor({ ToneMapNode() }) {
                    shaderNodeInput(ToneMapNode::inputColor)
                    shaderNodeOutput(ToneMapNode::result)
                }

                descriptor({ VelocityNode() }) {

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