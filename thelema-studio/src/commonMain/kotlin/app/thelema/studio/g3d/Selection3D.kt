package app.thelema.studio.g3d

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.ecs.sibling
import app.thelema.g3d.IScene
import app.thelema.g3d.ISceneInstance
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.IUniformArgs
import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.math.IMat4
import app.thelema.math.Vec4
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.shader.post.FXAA
import app.thelema.shader.post.PostShader
import app.thelema.shader.post.SobelFilter
import app.thelema.studio.Studio
import app.thelema.ui.Selection
import app.thelema.utils.Color
import app.thelema.utils.iterate

object Selection3D {
    var selection: Selection<IEntity>? = null

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

    val frameBuffer: IFrameBuffer by lazy { SimpleFrameBuffer() }
    val frameBuffer2: IFrameBuffer by lazy { SimpleFrameBuffer() }

    val sobel: SobelFilter by lazy { SobelFilter().apply { edgeColor.set(1f, 0.7f, 0.2f) } }

    val pixels: IByteData by lazy { DATA.bytes(GL.mainFrameBufferWidth * GL.mainFrameBufferHeight * 4) }

    val colorMap = HashMap<Int, IEntity>()

    val fxaa: FXAA by lazy { FXAA() }

    val colorUniform = StudioColorUniformNode()
    val selectionRenderShader = Shader {
        getOrCreateEntity()
        val vertex = VertexNode()
        vertex.maxBones = 100
        val camera = CameraDataNode()
        camera.vertexPosition = vertex.position
        val output = sibling<OutputNode>()
        output.vertPosition = camera.clipSpacePosition
        output.fragColor = colorUniform.uniform
        output.fadeStart = -1f
        rootNode = output.sibling()
    }

    val meshes = ArrayList<IMesh>()
    val scenes = ArrayList<ISceneInstance>()

    var selectRequest = false
    var pixelStart = 0

    fun select(x: Int, y: Int) {
        pixelStart = (y * GL.mainFrameBufferWidth + x) * 4
        selectRequest = true
    }

    private fun traverseSelection(entity: IEntity) {
        val scene = entity.componentOrNull<ISceneInstance>()?.also { scenes.add(it) }
        if (scene == null) entity.forEachChildEntity { traverseSelection(it) }
        entity.componentOrNull<IMesh>()?.also { meshes.add(it) }
    }

    fun prepareSelection() {
        meshes.clear()
        scenes.clear()

        val selection = selection
        if (selection?.isDisabled == false) {
            Studio.tabsPane.activeTab?.scene?.entity?.forEachChildEntity {
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

                    meshes.iterate { mesh ->
                        colorMap[colorId] = mesh.entity
                        Color.rgba8888ToColor(colorUniform.color, color.toInt())
                        mesh.render(selectionRenderShader)
                        color += 256
                        colorId++
                    }

                    scenes.iterate { scene ->
                        colorMap[colorId] = scene.entity
                        Color.rgba8888ToColor(colorUniform.color, color.toInt())
                        scene.sceneInstance?.componentOrNull<IScene>()?.also { instance ->
                            for (j in instance.renderables.indices) {
                                instance.renderables[j].render(selectionRenderShader)
                            }
                        }
                        color += 256
                        colorId++
                    }

                    GL.glReadPixels(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
                    GL.glClearColor(Color.BLACK)
                    GL.glClear()

                    val selectedColor = pixels[pixelStart].toInt() * 65025 + pixels[pixelStart + 1].toInt() * 255 + pixels[pixelStart + 2].toInt()

                    selection.choose(colorMap[selectedColor])
                }
            }
        }
    }

    fun render() {
        val selected = selection
        if (selected?.isDisabled == false) {
            if (selected.isNotEmpty()) {
                var worldMatrix: IMat4? = null

                var selection = false
                frameBuffer.render {
                    colorUniform.color.set(1f, 1f, 1f, 1f)
                    selected.iterate { selected ->
                        selected.componentOrNull<IMeshInstance>()?.also {
                            worldMatrix = it.worldMatrix
                            it.render(selectionRenderShader)
                            selection = true
                        }
                        selected.componentOrNull<ISceneInstance>()?.also { instance ->
                            instance.sceneInstance?.componentOrNull<IScene>()?.also { scene ->
                                scene.renderables.iterate { it.render(selectionRenderShader) }
                                if (scene.renderables.isNotEmpty()) selection = true
                            }
                        }
                        if (worldMatrix == null) worldMatrix = selected.componentOrNull<ITransformNode>()?.worldMatrix
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

                    fxaa.render(frameBuffer2.getTexture(0), frameBuffer)
                    selectionTransparencyShader.render(frameBuffer.getTexture(0))

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