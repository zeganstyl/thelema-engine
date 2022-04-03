package app.thelema.studio.g3d

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.ecs.componentOrNull
import app.thelema.ecs.forEachComponent
import app.thelema.ecs.sibling
import app.thelema.g3d.ISceneInstance
import app.thelema.g3d.IUniformArgs
import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.math.Vec4
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.shader.post.PostShader
import app.thelema.shader.post.SobelFilter
import app.thelema.studio.Studio
import app.thelema.studio.ecs.EntityTreeNode
import app.thelema.ui.ITreeNode
import app.thelema.ui.Selection
import app.thelema.utils.Color
import app.thelema.utils.iterate

object Selection3D {
    var selection: Selection<ITreeNode>? = null

    val selectionTransparencyShader: PostShader by lazy {
        PostShader(
            fragCode = """
varying vec2 uv;
uniform sampler2D tex;

void main() {
    vec4 color = texture2D(tex, uv);
    gl_FragColor = vec4(color.rgb, color.r);
}
"""
        )
    }

    val frameBuffer: IFrameBuffer by lazy { SimpleFrameBuffer(pixelFormat = GL_RGB, internalFormat = GL_RGB8) }
    val frameBuffer2: IFrameBuffer by lazy { SimpleFrameBuffer() }

    val sobel: SobelFilter by lazy { SobelFilter().apply {
        width = 1.25f
        edgeColor.set(1f, 0.7f, 0.2f) }
    }

    private var pixels: IByteData = DATA.bytes(GL.mainFrameBufferWidth * GL.mainFrameBufferHeight * 3)
        set(value) {
            field.destroy()
            field = value
        }

    val colorMap = HashMap<Int, EntityTreeNode>()

    val colorUniform = StudioColorUniformNode()
    val selectionRenderShader = Shader {
        getOrCreateEntity()
        val output = sibling<OutputNode>()
        output.fragColor = colorUniform.uniform
        output.fadeStart = -1f
        rootNode = output.sibling()
    }

    val nodes = ArrayList<EntityTreeNode>()

    var selectRequest = false
    var pixelStart = 0

    fun resizeScreen(width: Int, height: Int) {
        frameBuffer.setResolution(width, height)
        frameBuffer2.setResolution(width, height)
        pixels = DATA.bytes(frameBuffer.width * frameBuffer.height * 3)
    }

    fun select(x: Int, y: Int) {
        pixelStart = (y * frameBuffer.width + x) * 3
        selectRequest = true
    }

    private fun traverseSelection(entity: EntityTreeNode) {
        val scene = entity.entity.componentOrNull<ISceneInstance>()?.also { nodes.add(entity) }
        if (scene == null) {
            entity.entity.forEachComponent { if (it is IRenderable) nodes.add(entity) }
            entity.children.iterate {
                it as EntityTreeNode
                traverseSelection(it)
            }
        }
    }

    fun prepareSelection() {
        nodes.clear()

        val selection = selection
        if (selection?.isDisabled == false) {
            Studio.activeEntityTab?.scene?.entityTree?.rootNode?.also {
                traverseSelection(it)
            }

            if (selectRequest) {
                selectRequest = false

                frameBuffer.renderNoClear {
                    selectionRenderShader.bind()

                    GL.glClearColor(Color.BLACK)
                    GL.glClear()

                    colorMap.clear()

                    // render to main frame buffer and read pixels from it
                    var color = 0x000001FFL
                    var colorId = 1

                    nodes.iterate { node ->
                        node.entity.forEachComponent { obj ->
                            if (obj is ISceneInstance) {
                                colorMap[colorId] = node
                                Color.rgba8888ToColor(colorUniform.color, color.toInt())
                                obj.sceneInstance?.forEachComponentInBranch {
                                    if (it is IRenderable) {
                                        it.render(selectionRenderShader)
                                    }
                                }
                                color += 256
                                colorId++

                            } else if (obj is IRenderable) {
                                colorMap[colorId] = node
                                Color.rgba8888ToColor(colorUniform.color, color.toInt())
                                obj.render(selectionRenderShader)
                                color += 256
                                colorId++
                            }
                        }
                    }

                    GL.glReadPixels(0, 0, frameBuffer.width, frameBuffer.height, GL_RGB, GL_UNSIGNED_BYTE, pixels)
                    GL.glClearColor(Color.BLACK)
                    GL.glClear()

                    val selectedColor = pixels[pixelStart].toInt() * 65025 + pixels[pixelStart + 1].toInt() * 255 + pixels[pixelStart + 2].toInt()

                    val entity = colorMap[selectedColor]
                    selection.choose(entity)

                    colorMap.clear()
                    nodes.clear()
                }
            }
        }
    }

    fun render() {
        val selected = selection
        if (selected?.isDisabled == false) {
            if (selected.isNotEmpty()) {
                var selection = false
                frameBuffer.render {
                    colorUniform.color.set(1f, 1f, 1f, 1f)
                    selected.iterate { selected ->
                        selected as EntityTreeNode
                        selected.entity.forEachComponent {
                            if (it is ISceneInstance) {
                                it.sceneInstance?.forEachComponentInBranch {
                                    if (it is IRenderable) {
                                        it.render(selectionRenderShader)
                                        selection = true
                                    }
                                }
                            } else if (it is IRenderable) {
                                it.render(selectionRenderShader)
                                selection = true
                            }
                        }
                    }
                }

                if (selection) {
                    frameBuffer2.renderNoClear {
                        GL.glClearColor(0f, 0f, 0f, 0f)
                        GL.glClear()
                        sobel.render(frameBuffer.getTexture(0))
                    }

                    GL.isBlendingEnabled = true
                    GL.setupSimpleAlphaBlending()
                    val mask = ScreenQuad.defaultClearMask
                    ScreenQuad.defaultClearMask = null

                    selectionTransparencyShader.render(frameBuffer2.getTexture(0))

                    ScreenQuad.defaultClearMask = mask
                }
            }
        }
    }
}

class StudioColorUniformNode: ShaderNode() {
    override val componentName: String
        get() = "StudioColorUniformNode"

    val uniform: IShaderData = GLSLValue("color", GLSLType.Vec4).also { output(it) }

    val color = Vec4(1f)

    override fun declarationFrag(out: StringBuilder) {
        out.append("uniform ${uniform.typedRef};\n")
    }

    override fun bind(uniforms: IUniformArgs) {
        shader[uniform.ref] = color
    }
}