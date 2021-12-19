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

import app.thelema.img.ITexture2D
import app.thelema.img.Texture2D
import app.thelema.shader.node.*
import app.thelema.utils.LOG

class PBRShader(deferredRendering: Boolean = false): Shader() {
    constructor(deferredRendering: Boolean = false, block: PBRShader.() -> Unit): this(deferredRendering) {
        block(this)
        build()
        LOG.info(printCode())
    }

    val baseColorNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            if (pbrNode.baseColor == null) {
                pbrNode.baseColor = texColor
            } else {
                pbrNode.baseColor
            }
        }
        // TODO
    }

    val baseColorTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            pbrNode.baseColor = texColor
        }
    }

    val alphaTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            pbrNode.alpha = texColor
        }
    }

    val normalTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = true
            normalMapNode.normalColor = texColor
            normalMapNode.uv = uv
        }
    }

    val normalMapNode: NormalMapNode by lazy {
        NormalMapNode {
            pbrNode.normal = normal
        }
    }

    val metallicTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            pbrNode.metallic = texColor
        }
    }

    val roughnessTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            pbrNode.roughness = texColor
        }
    }

    val occlusionTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            pbrNode.occlusion = texColor
        }
    }

    val metallicRoughnessTextureNode: Texture2DNode by lazy {
        Texture2DNode {
            sRGB = false
            val splitNode = SplitVec4Node(texColor)
            pbrNode.occlusion = splitNode.x
            pbrNode.roughness = splitNode.y
            pbrNode.metallic = splitNode.z
        }
    }

    val pbrNode = PBRNode {}

    val toneMapNode = ToneMapNode(pbrNode.result)

    val outputNode: OutputNode by lazy {
        addNode(OutputNode {
            fragColor = toneMapNode.result
            fadeStart = -1f
        })
    }

    val gBufferOutputNode: GBufferOutputNode by lazy {
        addNode(GBufferOutputNode {

        })
    }

    init {
        if (deferredRendering) version = 330
        (if (deferredRendering) gBufferOutputNode else outputNode).apply {  }
    }

    private inline fun setTex(uri: String, block: ITexture2D.() -> Unit): ITexture2D =
        Texture2D(uri).apply(block)
    
    fun setBaseColorTexture(texture: ITexture2D) { baseColorTextureNode.texture = texture }
    fun setBaseColorTexture(uri: String): ITexture2D = setTex(uri) { setBaseColorTexture(this) }

    fun setAlphaTexture(texture: ITexture2D) { alphaTextureNode.texture = texture }
    fun setAlphaTexture(uri: String): ITexture2D = setTex(uri) { setAlphaTexture(this) }

    fun setNormalTexture(texture: ITexture2D) { normalTextureNode.texture = texture }
    fun setNormalTexture(uri: String): ITexture2D = setTex(uri) { setNormalTexture(this) }

    /** Image channels must be: R - occlusion, G - roughness, B - metallic. */
    fun setOcclusionRoughnessMetallicTexture(texture: ITexture2D) { metallicRoughnessTextureNode.texture = texture }
    fun setOcclusionRoughnessMetallicTexture(uri: String): ITexture2D = setTex(uri) { setOcclusionRoughnessMetallicTexture(this) }

    fun setMetallicTexture(texture: ITexture2D) { metallicTextureNode.texture = texture }
    fun setMetallicTexture(uri: String): ITexture2D = setTex(uri) { setMetallicTexture(this) }

    fun setRoughnessTexture(texture: ITexture2D) { roughnessTextureNode.texture = texture }
    fun setRoughnessTexture(uri: String): ITexture2D = setTex(uri) { setRoughnessTexture(this) }

    fun setOcclusionTexture(texture: ITexture2D) { occlusionTextureNode.texture = texture }
    fun setOcclusionTexture(uri: String): ITexture2D = setTex(uri) { setOcclusionTexture(this) }

    fun enableShadows(enable: Boolean = true) { pbrNode.receiveShadows = enable }
}