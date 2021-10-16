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

import app.thelema.img.ITexture
import app.thelema.img.Texture2D
import app.thelema.math.TransformDataType
import app.thelema.shader.node.*

class PBRShader(deferredRendering: Boolean = false): Shader() {
    constructor(deferredRendering: Boolean = false, block: PBRShader.() -> Unit): this(deferredRendering) {
        block(this)
        build()
    }

    val vertexNode = addNode(VertexNode())
    val cameraDataNode = addNode(CameraDataNode(vertexNode.position))

    val uvNode = addNode(UVNode {
        aUVName = "UV"
    })

    val baseColorNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            if (pbrNode.baseColor == null) {
                pbrNode.baseColor = color
            } else {
                pbrNode.baseColor
            }
        }
        // TODO
    }

    val baseColorTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            pbrNode.baseColor = color
        }
    }

    val alphaTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            pbrNode.alpha = color
        }
    }

    val normalTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = true).apply {
            normalMapNode.normalColor = color
            normalMapNode.uv = uv
        })
    }

    val normalMapNode: NormalMapNode by lazy {
        addNode(NormalMapNode {
            vertexPosition = vertexNode.position
            tbn = vertexNode.tbn
            normalizedViewVector = cameraDataNode.normalizedViewVector

            pbrNode.normal = normalResult
        })
    }

    val metallicTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            pbrNode.metallic = color
        }
    }

    val roughnessTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            pbrNode.roughness = color
        }
    }

    val occlusionTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            pbrNode.occlusion = color
        }
    }

    val metallicRoughnessTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            val splitNode = addNode(SplitVec4Node(color))
            pbrNode.occlusion = splitNode.x
            pbrNode.roughness = splitNode.y
            pbrNode.metallic = splitNode.z
        }
    }

    val pbrNode = addNode(PBRNode {
        worldPosition = vertexNode.position
        normalizedViewVector = cameraDataNode.normalizedViewVector
        clipSpacePosition = cameraDataNode.clipSpacePosition
    })

    val toneMapNode = addNode(ToneMapNode(pbrNode.result))

    val outputNode: OutputNode by lazy {
        addNode(OutputNode {
            vertPosition = cameraDataNode.clipSpacePosition
            fragColor = toneMapNode.result
            fadeStart = -1f
        })
    }

    val gBufferOutputNode: GBufferOutputNode by lazy {
        addNode(GBufferOutputNode {
            vertPosition = cameraDataNode.clipSpacePosition
            fragColor = GLSL.oneFloat
            fragNormal = vertexNode.normal
            fragPosition = cameraDataNode.viewSpacePosition
        })
    }

    init {
        if (deferredRendering) version = 330
        (if (deferredRendering) gBufferOutputNode else outputNode).apply {  }
    }

    fun setupBones(bonesNum: Int) {
        vertexNode.maxBones = bonesNum
    }

    fun disableTransformation() {
        vertexNode.worldTransformType = TransformDataType.None
    }

    private inline fun setTex(uri: String, block: ITexture.() -> Unit): ITexture =
        Texture2D(uri).apply(block)
    
    fun setBaseColorTexture(texture: ITexture) { baseColorTextureNode.texture = texture }
    fun setBaseColorTexture(uri: String): ITexture = setTex(uri) { setBaseColorTexture(this) }

    fun setAlphaTexture(texture: ITexture) { alphaTextureNode.texture = texture }
    fun setAlphaTexture(uri: String): ITexture = setTex(uri) { setAlphaTexture(this) }

    fun setNormalTexture(texture: ITexture) { normalTextureNode.texture = texture }
    fun setNormalTexture(uri: String): ITexture = setTex(uri) { setNormalTexture(this) }

    /** Image channels must be: R - occlusion, G - roughness, B - metallic. */
    fun setOcclusionRoughnessMetallicTexture(texture: ITexture) { metallicRoughnessTextureNode.texture = texture }
    fun setOcclusionRoughnessMetallicTexture(uri: String): ITexture = setTex(uri) { setOcclusionRoughnessMetallicTexture(this) }

    fun setMetallicTexture(texture: ITexture) { metallicTextureNode.texture = texture }
    fun setMetallicTexture(uri: String): ITexture = setTex(uri) { setMetallicTexture(this) }

    fun setRoughnessTexture(texture: ITexture) { roughnessTextureNode.texture = texture }
    fun setRoughnessTexture(uri: String): ITexture = setTex(uri) { setRoughnessTexture(this) }

    fun setOcclusionTexture(texture: ITexture) { occlusionTextureNode.texture = texture }
    fun setOcclusionTexture(uri: String): ITexture = setTex(uri) { setOcclusionTexture(this) }

    fun enableShadows(enable: Boolean = true) { pbrNode.receiveShadows = enable }
}