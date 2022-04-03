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
import app.thelema.app.AppListener
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.ecs.sibling
import app.thelema.g3d.ShaderChannel
import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.shader.post.*
import app.thelema.utils.Color

// FIXME problem with changing window size
class ForwardRenderingPipeline: IRenderingPipeline {
    override val componentName: String
        get() = "ForwardRenderingPipeline"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            sibling<RenderingPipeline>().proxy = this
            value?.also {
                fxaa = value.component()
                bloom = value.entity("Bloom").component()
                bloom.apply { brightnessMap = buffer3.getTexture(0) }
                bloomThreshold = value.entity("Bloom").component()
                motionBlur = value.component()
                motionBlur.apply { velocityMap = buffer3.getTexture(0) }
                godRays = value.entity("GodRays").component()
                godRays.apply { occlusionMap = buffer3.getTexture(0) }
                godRaysThreshold = value.entity("GodRays").component()
                vignette = value.component()
            }
        }

    var height: Int = APP.height
    var width: Int = APP.width

    val buffer1 = FrameBuffer {
        addAttachment(Attachments.color(0, GL_RGBA, GL_RGBA16F, GL_FLOAT))
        addAttachment(Attachments.depthRenderBuffer())
        buildAttachments()
    }
    val buffer2 = FrameBuffer {
        addAttachment(Attachments.color(0, GL_RGBA, GL_RGBA16F, GL_FLOAT))
        addAttachment(Attachments.depthRenderBuffer())
        buildAttachments()
    }
    val buffer3 = FrameBuffer {
        addAttachment(Attachments.color(0, GL_RGBA, GL_RGBA16F, GL_FLOAT))
        addAttachment(Attachments.depthRenderBuffer())
        buildAttachments()
    }

    val swapper: FrameBufferSwapper by lazy { FrameBufferSwapper(buffer1, buffer2) }

    var fxaaEnabled = true
    var vignetteEnabled = true
    var bloomEnabled = true
    var motionBlurEnabled = true
    var godRaysEnabled = false

    var fxaa = FXAA()
        set(value) {
            field.destroy()
            field = value
        }

    var vignette = Vignette()
        set(value) {
            field.destroy()
            field = value
        }

    var bloomThreshold = Threshold()
        set(value) {
            field.destroy()
            field = value
        }

    var bloom = Bloom(width, height).apply { brightnessMap = buffer3.getTexture(0) }
        set(value) {
            field.destroy()
            field = value
        }

    var motionBlur = MotionBlur(numSamples = 16).apply { velocityMap = buffer3.getTexture(0) }
        set(value) {
            field.destroy()
            field = value
        }

    var godRays = GodRays().apply { occlusionMap = buffer3.getTexture(0) }
        set(value) {
            field.destroy()
            field = value
        }

    var godRaysThreshold = Threshold()
        set(value) {
            field.destroy()
            field = value
        }

    init {
        APP.addListener(object : AppListener {
            override fun resized(width: Int, height: Int) {
                setResolution(width, height)
                updateResolution()
            }
        })
    }

    override fun setResolution(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun updateResolution() {
        buffer1.setResolution(width, height)
        buffer2.setResolution(width, height)
        buffer3.setResolution(width, height)
    }

    override fun render(block: (shaderChannel: String?) -> Unit) {
        GL.glClearColor(Color.BLACK)

        swapper.reset()

        if (APP.isWeb) {
            swapper.current.render { block(null) }
        } else {
            GL.glBindFramebuffer(GL_FRAMEBUFFER, GL.mainFrameBufferHandle)
            GL.glClear()
            block(null)

            GL.glBindFramebuffer(GL_READ_FRAMEBUFFER, GL.mainFrameBufferHandle)
            swapper.current.bindDraw()
            GL.glBlitFramebuffer(
                0,
                0,
                GL.mainFrameBufferWidth,
                GL.mainFrameBufferHeight,
                0,
                0,
                swapper.current.width,
                swapper.current.height,
                GL_COLOR_BUFFER_BIT,
                GL_NEAREST
            )
            GL.glBindFramebuffer(GL_FRAMEBUFFER, GL.mainFrameBufferHandle)
        }

        val isDepthTestEnabled = GL.isDepthTestEnabled
        GL.isDepthTestEnabled = false

        if (bloomEnabled) {
            bloomThreshold.render(swapper.currentTexture, buffer3)
            swapper.render(bloom)
        }

        if (godRaysEnabled) {
            swapper.next.render {
                //renderGodRaysScene()
            }

            godRaysThreshold.render(swapper.next.getTexture(0), buffer3)
            swapper.render(godRays)
        }

        if (motionBlurEnabled) {
            buffer3.render { block(ShaderChannel.Velocity) }
            swapper.render(motionBlur)
        }

        if (vignetteEnabled) {
            swapper.render(vignette)
        }

        if (fxaaEnabled) {
            swapper.render(fxaa)
        }

        ScreenQuad.render(swapper.currentTexture, false, clearMask = null)

        GL.isDepthTestEnabled = isDepthTestEnabled
    }
}