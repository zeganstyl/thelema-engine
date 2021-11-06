package app.thelema.studio

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.IScene
import app.thelema.g3d.ITransformNode
import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.math.IMat4
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.post.FXAA
import app.thelema.shader.post.PostShader
import app.thelema.shader.post.SobelFilter
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

    val selectionRenderShader: SimpleShader3D by lazy {
        SimpleShader3D {
            alphaCutoff = 0f
            lightDirection = null
            renderAttributeName = ""
            color = Vec4()
        }
    }

    val meshes = ArrayList<IMesh>()
    val scenes = ArrayList<IScene>()

    var selectRequest = false
    var pixelStart = 0

    fun select(x: Int, y: Int) {
        pixelStart = (y * GL.mainFrameBufferWidth + x) * 4
        selectRequest = true
    }

    private fun traverseSelection(entity: IEntity) {
        val scene = entity.componentOrNull<IScene>()?.also { scenes.add(it) }
        if (scene == null) entity.forEachChildEntity { traverseSelection(it) }
        entity.componentOrNull<IMesh>()?.also { meshes.add(it) }
    }

    fun prepareSelection() {
        meshes.clear()
        scenes.clear()

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
                    selectionRenderShader.color?.also { Color.rgba8888ToColor(it, color.toInt()) }
                    mesh.render(selectionRenderShader)
                    color += 256
                    colorId++
                }

                scenes.iterate { scene ->
                    colorMap[colorId] = scene.entity
                    selectionRenderShader.color?.also { Color.rgba8888ToColor(it, color.toInt()) }
                    for (j in scene.meshes.indices) {
                        scene.meshes[j].render(selectionRenderShader)
                    }
                    color += 256
                    colorId++
                }

                GL.glReadPixels(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
                GL.glClearColor(Color.BLACK)
                GL.glClear()

                val selectedColor = pixels[pixelStart].toInt() * 65025 + pixels[pixelStart + 1].toInt() * 255 + pixels[pixelStart + 2].toInt()

                selection?.choose(colorMap[selectedColor])
            }
        }
    }

    fun render() {
        selection?.also { selected ->
            if (selected.isNotEmpty()) {
                var worldMatrix: IMat4? = null

                var selection = false
                frameBuffer.render {
                    selectionRenderShader.color?.set(1f, 1f, 1f, 1f)
                    selected.iterate { selected ->
                        selected.componentOrNull<IMesh>()?.also {
                            worldMatrix = it.worldMatrix
                            it.render(selectionRenderShader)
                            selection = true
                        }
                        selected.componentOrNull<IScene>()?.apply {
                            for (i in meshes.indices) {
                                meshes[i].render(selectionRenderShader)
                            }
                            if (meshes.isNotEmpty()) selection = true
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