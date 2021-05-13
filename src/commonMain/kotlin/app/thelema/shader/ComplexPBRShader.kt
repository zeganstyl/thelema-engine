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
import app.thelema.math.TransformDataType
import app.thelema.shader.node.*

class ComplexPBRShader(): Shader() {
    constructor(block: ComplexPBRShader.() -> Unit): this() {
        block(this)
        build()
    }

    val vertexNode = addNode(VertexNode())
    val cameraDataNode = addNode(CameraDataNode(vertexNode.position))

    val uvNode = addNode(UVNode {
        aUVName = "UV"
    })

    val colorTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            principledBSDF.baseColor = color
        }
    }

    val normalTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false).apply {
            normalMapNode.normalColor = color
            normalMapNode.uv = uv
        })
    }

    val normalMapNode: NormalMapNode by lazy {
        addNode(NormalMapNode {
            vertexPosition = vertexNode.position
            tbn = vertexNode.tbn
            normalizedViewVector = cameraDataNode.normalizedViewVector

            principledBSDF.normal = normalResult
        })
    }

    val metallicRoughnessTextureNode: TextureNode by lazy {
        addNode(TextureNode(uvNode.uv, sRGB = false)).apply {
            val splitNode = addNode(SplitVec4Node(color))
            principledBSDF.occlusion = splitNode.x
            principledBSDF.roughness = splitNode.y
            principledBSDF.metallic = splitNode.z
        }
    }

    val principledBSDF = addNode(PrincipledBSDF {
        worldPosition = vertexNode.position
        normalizedViewVector = cameraDataNode.normalizedViewVector
        clipSpacePosition = cameraDataNode.clipSpacePosition
    })

    val toneMapNode = addNode(ToneMapNode(principledBSDF.result))

    val outputNode = addNode(OutputNode {
        vertPosition = cameraDataNode.clipSpacePosition
        fragColor = toneMapNode.result
        fadeStart = -1f
    })

    fun setupBones(bonesNum: Int) {
        vertexNode.maxBones = bonesNum
    }

    fun disableTransformation() {
        vertexNode.worldTransformType = TransformDataType.None
    }
    
    fun setColorTexture(texture: ITexture) {
        colorTextureNode.texture = texture
    }

    fun setNormalTexture(texture: ITexture) {
        normalTextureNode.texture = texture
    }

    /** Image channels must be: R - occlusion, G - roughness, B - metallic. */
    fun setMetallicRoughnessTexture(texture: ITexture) {
        metallicRoughnessTextureNode.texture = texture
    }

    fun enableShadows(enable: Boolean = true) {
        principledBSDF.receiveShadows = enable
    }
}