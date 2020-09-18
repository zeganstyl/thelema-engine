/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.g3d.Blending
import org.ksdfv.thelema.g3d.IMaterial
import org.ksdfv.thelema.g3d.ShaderChannel
import org.ksdfv.thelema.gl.GL_BACK
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.math.Vec4
import org.ksdfv.thelema.shader.IShader
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.node.*

/** @author zeganstyl */
interface IGLTFMaterial: IGLTFArrayElement {
    var material: IMaterial

    var alphaMode: String
    var alphaCutoff: Float
    var doubleSided: Boolean

    var baseColorTexture: Int
    var baseColorTextureUV: Int
    var baseColor: IVec4?
    var metallic: Float
    var roughness: Float
    var metallicRoughnessTexture: Int
    var metallicRoughnessTextureUV: Int

    var occlusionStrength: Float
    var occlusionTexture: Int
    var occlusionTextureUV: Int

    var normalScale: Float
    var normalTexture: Int
    var normalTextureUV: Int

    var emissiveFactor: IVec3
    var emissiveTexture: Int
    var emissiveTextureUV: Int

    private fun getOrCreateUVNode(shader: IShader, attributeName: String, map: MutableMap<String, UVNode>): UVNode {
        var node = map[attributeName]
        if (node == null) {
            node = shader.addNode(UVNode())
            node.aUVName = attributeName
            map[attributeName] = node
        }
        return node
    }

    fun buildShaders() {
        material.shaderChannels.values.forEach { it.build() }
    }

    override fun read(json: IJsonObject) {
        val material = material
        json.string("name") {
            name = it
            material.name = it
        }

        val uvNodes = HashMap<String, UVNode>()

        val shader = Shader(version = gltf.conf.shaderVersion)
        shader.name = name

        material.shader = shader
        material.shaderChannels[ShaderChannel.Default] = shader

        json.string("alphaMode") { alphaMode = it }
        when (alphaMode) {
            "OPAQUE" -> {
                material.alphaMode = Blending.Opaque
            }
            "MASK" -> {
                alphaCutoff = json.float("alphaCutoff", 0.5f)
                material.alphaCutoff = alphaCutoff
                material.alphaMode = Blending.Clip
            }
            "BLEND" -> {
                material.alphaMode = Blending.Blend
            }
        }

        json.bool("doubleSided") { doubleSided = it }
        material.cullFaceMode = if (doubleSided) 0 else GL_BACK

        val vertexNode = shader.addNode(VertexNode())
        vertexNode.bonesName = "JOINTS_"
        vertexNode.boneWeightsName = "WEIGHTS_"
        vertexNode.positionName = "POSITION"
        vertexNode.normalName = "NORMAL"
        vertexNode.tangentName = "TANGENT"

        val cameraDataNode = shader.addNode(CameraDataNode(vertexNode.position))

        var normalValue: IShaderData = vertexNode.normal
        json.get("normalTexture") {
            float("scale") { normalScale = it }

            int("texCoord") { normalTextureUV = it }
            val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$normalTextureUV", uvNodes)

            normalTexture = int("index")
            val textureNode = shader.addNode(TextureNode(uvNode.uv, gltf.textures[normalTexture].texture, false))

            shader.addNode(NormalMapNode(vertexNode.position).apply {
                uv = textureNode.uv
                tbn = vertexNode.tbn
                normalizedViewVector = cameraDataNode.normalizedViewVector
                normalColor = textureNode.color

                normalValue = normalResult
            })
        }

        var baseColorValue: IShaderData = GLSL.oneFloat
        var occlusionValue: IShaderData = GLSL.oneFloat
        var roughnessValue: IShaderData = GLSL.oneFloat
        var metallicValue: IShaderData = GLSL.zeroFloat

        val alphaNodes = ArrayList<IShaderNode>()

        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-pbrmetallicroughness
        json.get("pbrMetallicRoughness") {
            get("baseColorTexture") {
                int("texCoord") { baseColorTextureUV = it }
                val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$baseColorTextureUV", uvNodes)

                alphaNodes.add(uvNode)

                baseColorTexture = int("index")
                val textureNode = shader.addNode(TextureNode(uvNode.uv, gltf.textures[baseColorTexture].texture))
                alphaNodes.add(textureNode)
                baseColorValue = textureNode.color
            }

            array("baseColorFactor") {
                val color = Vec4(
                    float(0, 0f),
                    float(1, 0f),
                    float(2, 0f),
                    float(3, 0f)
                )

                baseColor = color

                val factor = GLSLVec4Inline(color.x, color.y, color.z, color.w)
                baseColorValue = if (baseColorValue !== GLSL.oneFloat) {
                    val node = shader.addNode(OperationNode(listOf(baseColorValue, factor), "arg1 * arg2", GLSLType.Vec4))
                    alphaNodes.add(node)
                    node.result
                } else {
                    factor
                }
            }

            get("metallicRoughnessTexture") {
                int("texCoord") { metallicRoughnessTextureUV = it }
                val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$metallicRoughnessTextureUV", uvNodes)

                metallicRoughnessTexture = int("index")
                val textureNode = shader.addNode(TextureNode(uvNode.uv, gltf.textures[metallicRoughnessTexture].texture, false))
                val splitNode = shader.addNode(SplitVec4Node(textureNode.color))

                occlusionValue = splitNode.x
                roughnessValue = splitNode.y
                metallicValue = splitNode.z
            }

            float("roughnessFactor") {
                roughness = it
                val factor = GLSLFloatInline(it)
                roughnessValue = if (roughnessValue !== GLSL.oneFloat) {
                    shader.addNode(OperationNode(listOf(roughnessValue, factor), "arg1 * arg2", GLSLType.Float)).result
                } else {
                    factor
                }
            }

            float("metallicFactor") {
                metallic = it
                val factor = GLSLFloatInline(it)
                metallicValue = if (metallicValue !== GLSL.zeroFloat) {
                    shader.addNode(OperationNode(listOf(metallicValue, factor), "arg1 * arg2", GLSLType.Float)).result
                } else {
                    factor
                }
            }
        }

        val principledBSDF = shader.addNode(PrincipledBSDF().apply {
            normal = normalValue
            worldPosition = vertexNode.position
            normalizedViewVector = cameraDataNode.normalizedViewVector
            baseColor = baseColorValue
            occlusion = occlusionValue
            roughness = roughnessValue
            metallic = metallicValue
            receiveShadows = gltf.conf.receiveShadows
            clipSpacePosition = cameraDataNode.clipSpacePosition
        })

        val toneMapNode = shader.addNode(ToneMapNode(principledBSDF.result))

        if (gltf.conf.setupGBufferShader) {
            val outputNode = shader.addNode(GBufferOutputNode().apply {
                vertPosition = cameraDataNode.clipSpacePosition
                fragColor = toneMapNode.result
                fragNormal = normalValue
                fragPosition = cameraDataNode.viewSpacePosition
            }
            )

            outputNode.alphaCutoff = material.alphaCutoff
            outputNode.alphaMode = material.alphaMode
            outputNode.cullFaceMode = material.cullFaceMode
        } else {
            val outputNode = shader.addNode(OutputNode(cameraDataNode.clipSpacePosition, toneMapNode.result))
            outputNode.alphaCutoff = material.alphaCutoff
            outputNode.alphaMode = material.alphaMode
            outputNode.cullFaceMode = material.cullFaceMode
        }

        if (gltf.conf.setupVelocityShader) {
            material.shaderChannels[ShaderChannel.Velocity] = Shader().apply {
                addNode(vertexNode)

                val blurCameraDataNode = addNode(CameraDataNode(vertexNode.position))
                val velocityNode = addNode(
                    VelocityNode(
                        vertexNode.position,
                        blurCameraDataNode.clipSpacePosition,
                        blurCameraDataNode.previousViewProjectionMatrix,
                        vertexNode.normal
                    )
                )
                velocityNode.aBonesName = "JOINTS_"
                velocityNode.aBoneWeightsName = "WEIGHTS_"
                velocityNode.aPositionName = "POSITION"

                val alphaOp = addNode(
                    OperationNode(
                        listOf(velocityNode.velocity, baseColorValue),
                        "vec4(arg1.xy, 0.0, arg2.a)",
                        GLSLType.Vec4
                    )
                )
                alphaOp.isVarying = false
                alphaOp.isFragment = true

                nodes.addAll(alphaNodes)

                val outputNode = addNode(OutputNode(velocityNode.stretchedClipSpacePosition, alphaOp.result))
                outputNode.alphaCutoff = material.alphaCutoff
                outputNode.alphaMode = material.alphaMode
                outputNode.cullFaceMode = material.cullFaceMode
            }
        }

        if (gltf.conf.setupDepthRendering) {
            material.shaderChannels[ShaderChannel.Depth] = Shader().apply {
                addNode(vertexNode)

                val depthCameraDataNode = addNode(CameraDataNode(vertexNode.position))

                nodes.addAll(alphaNodes)

                val outputNode = addNode(OutputNode(depthCameraDataNode.clipSpacePosition, baseColorValue))
                outputNode.alphaCutoff = material.alphaCutoff
                outputNode.alphaMode = material.alphaMode
                outputNode.cullFaceMode = material.cullFaceMode
            }
        }

        json.get("occlusionTexture") {
            float("strength") { occlusionStrength = it }

            int("texCoord") { occlusionTextureUV = it }
            val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$occlusionTextureUV", uvNodes)

            occlusionTexture = int("index")
            val textureNode = shader.addNode(TextureNode(uvNode.uv, gltf.textures[occlusionTexture].texture, false))
            principledBSDF.occlusion = textureNode.color
            //float("strength", 1f)
        }

        if (json.contains("emissiveTexture") || json.contains("emissiveFactor")) {
            var emissiveValue: IShaderData = GLSL.zeroFloat

            json.get("emissiveTexture") {
                int("texCoord") { emissiveTextureUV = it }
                val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$emissiveTextureUV", uvNodes)

                emissiveTexture = int("index")
                val textureNode = shader.addNode(TextureNode(uvNode.uv, gltf.textures[emissiveTexture].texture))
                emissiveValue = textureNode.color
            }

            json.array("emissiveFactor") {
                val color = Vec3(
                    float(0),
                    float(1),
                    float(2)
                )

                emissiveFactor = color

                val factor = GLSLVec3Inline(color.x, color.y, color.z)
                emissiveValue = if (emissiveValue !== GLSL.zeroFloat) {
                    val op = OperationNode(listOf(emissiveValue, factor), "arg1.xyz * arg2", GLSLType.Vec3)
                    op.isFragment = true
                    op.isVarying = false
                    val node = shader.addNode(op)
                    node.result
                } else {
                    factor
                }
            }

            if (emissiveValue !== GLSL.zeroFloat) {
                val opNode = shader.addNode(
                    OperationNode(
                        args = listOf(principledBSDF.result, emissiveValue),
                        function = "vec4(arg1.xyz + arg2.xyz, arg1.a)",
                        resultType = GLSLType.Vec4
                    )
                )
                opNode.isFragment = true
                opNode.isVarying = false

                toneMapNode.inputColor = opNode.result
            }
        }
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name
        if (alphaMode != "OPAQUE") json["alphaMode"] = alphaMode
        if (alphaCutoff != 0.5f) json["alphaCutoff"] = alphaCutoff
        if (doubleSided) json["doubleSided"] = doubleSided

        if (normalTexture != -1) {
            json.set("normalTexture") {
                set("index", normalTexture)
                if (normalTextureUV != 0) set("texCoord", normalTextureUV)
                if (normalScale != 1f) set("scale", normalScale)
            }
        }

        json.set("pbrMetallicRoughness") {
            if (baseColorTexture != -1) set("baseColorTexture") {
                set("index", baseColorTexture)
                if (baseColorTextureUV != 0) set("texCoord", baseColorTextureUV)
            }

            val baseColor = baseColor
            if (baseColor != null) {
                setArray("baseColorFactor") {
                    add(
                        baseColor.r,
                        baseColor.g,
                        baseColor.b,
                        baseColor.a
                    )
                }
            }

            if (metallicRoughnessTexture != -1) {
                set("metallicRoughnessTexture") {
                    set("index", metallicRoughnessTexture)
                    if (metallicRoughnessTextureUV != 0) set("texCoord", metallicRoughnessTextureUV)
                }
            }
            set("roughnessFactor", roughness)
            set("metallicFactor", metallic)
        }

        if (occlusionTexture != -1) {
            json.set("occlusionTexture") {
                set("index", occlusionTexture)
                if (occlusionTextureUV != 0) set("texCoord", occlusionTextureUV)
                if (occlusionStrength != 1f) set("strength", occlusionStrength)
            }
        }

        if (emissiveTexture != -1) {
            json.set("emissiveTexture") {
                set("index", emissiveTexture)
                if (emissiveTextureUV != 0) set("texCoord", emissiveTextureUV)
            }
        }

        val emi = emissiveFactor
        if (emi.x != 0f || emi.y != 0f || emi.z != 0f) {
            json.setArray("emissiveFactor") {
                add(emi.x, emi.y, emi.z)
            }
        }
    }

    override fun destroy() {}
}