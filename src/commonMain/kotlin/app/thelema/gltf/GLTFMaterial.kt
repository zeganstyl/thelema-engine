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

package app.thelema.gltf

import app.thelema.ecs.component
import app.thelema.g3d.Blending
import app.thelema.g3d.IMaterial
import app.thelema.g3d.ShaderChannel
import app.thelema.gl.GL_BACK
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.node.*

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-material)
 *
 * @author zeganstyl */
class GLTFMaterial(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var alphaMode: String = "OPAQUE"
    var alphaCutoff: Float = 0.5f
    var doubleSided = false

    var baseColorTexture: Int = -1
    var baseColorTextureUV: Int = 0
    var baseColor: IVec4? = null
    var metallic: Float = 0f
    var roughness: Float = 1f
    var metallicRoughnessTexture: Int = -1
    var metallicRoughnessTextureUV: Int = 0

    var occlusionStrength: Float = 1f
    var occlusionTexture: Int = -1
    var occlusionTextureUV: Int = 0

    var normalScale: Float = 1f
    var normalTexture: Int = -1
    var normalTextureUV: Int = 0

    var emissiveFactor: IVec3 = MATH.Zero3
    var emissiveTexture: Int = -1
    var emissiveTextureUV: Int = 0

    override val defaultName: String
        get() = "Material"

    val material: IMaterial = gltf.materialsEntity.entity("${defaultName}_$elementIndex").component()

    private fun getOrCreateUVNode(shader: IShader, attributeName: String, map: MutableMap<String, UVNode>): UVNode {
        var node = map[attributeName]
        if (node == null) {
            node = shader.addNode(UVNode())
            node.uvName = attributeName
            map[attributeName] = node
        }
        return node
    }

    fun buildShaders() {
        material.shaderChannels.values.forEach { it.build() }
    }

    override fun readJson() {
        super.readJson()

        material.entity.name = name

        val uvNodes = HashMap<String, UVNode>()

        val shader = Shader(version = if (gltf.conf.ibl && gltf.conf.shaderVersion < 130) 130 else gltf.conf.shaderVersion)
        shader.name = name

        material.shader = shader
        material.shaderChannels[ShaderChannel.Default] = shader

        json.string("alphaMode") { alphaMode = it }
        when (alphaMode) {
            "OPAQUE" -> {
                material.alphaMode = Blending.OPAQUE
            }
            "MASK" -> {
                alphaCutoff = json.float("alphaCutoff", 0.5f)
                material.alphaMode = Blending.MASK
            }
            "BLEND" -> {
                material.alphaMode = Blending.BLEND
            }
        }

        doubleSided = json.bool("doubleSided", false)
        val cullFaceMode = if (doubleSided) 0 else GL_BACK

        val vertexNode = shader.addNode(VertexNode {
            bonesName = "JOINTS_"
            boneWeightsName = "WEIGHTS_"
            positionName = "POSITION"
            normalName = "NORMAL"
            tangentName = "TANGENT"
        })

        val cameraDataNode = shader.addNode(CameraDataNode(vertexNode.position))

        var normalValue: IShaderData = vertexNode.normal
        json.get("normalTexture") {
            float("scale") { normalScale = it }

            normalTextureUV = int("texCoord", 0)
            val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$normalTextureUV", uvNodes)

            normalTexture = int("index")
            val textureNode = shader.addNode(TextureNode(uvNode.uv, (gltf.textures[normalTexture] as GLTFTexture).texture, false))

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
                baseColorTextureUV = int("texCoord", 0)
                val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$baseColorTextureUV", uvNodes)

                alphaNodes.add(uvNode)

                baseColorTexture = int("index")
                val textureNode = shader.addNode(TextureNode(uvNode.uv, (gltf.textures[baseColorTexture] as GLTFTexture).texture))
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
                    val node = shader.addNode(Op2Node(baseColorValue, factor, "in1 * in2", GLSLType.Vec4))
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
                val textureNode = shader.addNode(TextureNode(uvNode.uv, (gltf.textures[metallicRoughnessTexture] as GLTFTexture).texture, false))
                val splitNode = shader.addNode(SplitVec4Node(textureNode.color))

                occlusionValue = splitNode.x
                roughnessValue = splitNode.y
                metallicValue = splitNode.z
            }

            float("roughnessFactor") {
                roughness = it
                val factor = GLSLFloatInline(it)
                roughnessValue = if (roughnessValue !== GLSL.oneFloat) {
                    shader.addNode(Op2Node(roughnessValue, factor, "in1 * in2", GLSLType.Float)).result
                } else {
                    factor
                }
            }

            float("metallicFactor") {
                metallic = it
                val factor = GLSLFloatInline(it)
                metallicValue = if (metallicValue !== GLSL.zeroFloat) {
                    shader.addNode(Op2Node(metallicValue, factor, "in1 * in2", GLSLType.Float)).result
                } else {
                    factor
                }
            }
        }

        val pbrNode = shader.addNode(PBRNode().apply {
            normal = normalValue
            worldPosition = vertexNode.position
            normalizedViewVector = cameraDataNode.normalizedViewVector
            baseColor = baseColorValue
            occlusion = occlusionValue
            roughness = roughnessValue
            metallic = metallicValue
            receiveShadows = gltf.conf.receiveShadows
            clipSpacePosition = cameraDataNode.clipSpacePosition
            iblEnabled = gltf.conf.ibl
            iblMaxMipLevels = gltf.conf.iblMaxMipLevels
        })

        val toneMapNode = shader.addNode(ToneMapNode(pbrNode.result))

        if (gltf.conf.setupGBufferShader) {
            val outputNode = shader.addNode(GBufferOutputNode().apply {
                vertPosition = cameraDataNode.clipSpacePosition
                fragColor = toneMapNode.result
                fragNormal = normalValue
                fragPosition = cameraDataNode.viewSpacePosition
            })

            outputNode.alphaCutoff = alphaCutoff
            outputNode.alphaMode = material.alphaMode
            outputNode.cullFaceMode = cullFaceMode
        } else {
            val outputNode = shader.addNode(OutputNode(cameraDataNode.clipSpacePosition, toneMapNode.result))
            outputNode.alphaCutoff = alphaCutoff
            outputNode.alphaMode = material.alphaMode
            outputNode.cullFaceMode = cullFaceMode
        }

        if (gltf.conf.setupVelocityShader) {
            material.shaderChannels[ShaderChannel.Velocity] = Shader(version = gltf.conf.shaderVersion).apply {
                addNode(vertexNode)

                val blurCameraDataNode = addNode(CameraDataNode(vertexNode.position))
                val velocityNode = addNode(VelocityNode {
                    worldSpacePosition = vertexNode.position
                    clipSpacePosition = blurCameraDataNode.clipSpacePosition
                    previousViewProjectionMatrix = blurCameraDataNode.previousViewProjectionMatrix
                    normal = vertexNode.normal
                    aBonesName = "JOINTS_"
                    aBoneWeightsName = "WEIGHTS_"
                    aPositionName = "POSITION"
                })

                val alphaOp = addNode(
                    Op2Node(
                        velocityNode.velocity, baseColorValue,
                        "vec4(in1.xy, 0.0, in2.a)",
                        GLSLType.Vec4
                    )
                )
                alphaOp.isVarying = false
                alphaOp.isFragment = true

                nodes.addAll(alphaNodes)

                val outputNode = addNode(OutputNode(velocityNode.stretchedClipSpacePosition, alphaOp.result))
                outputNode.alphaCutoff = alphaCutoff
                outputNode.alphaMode = material.alphaMode
                outputNode.cullFaceMode = cullFaceMode
            }
        }

        if (gltf.conf.setupDepthRendering) {
            material.shaderChannels[ShaderChannel.Depth] = Shader(version = gltf.conf.shaderVersion).apply {
                addNode(vertexNode)

                val depthCameraDataNode = addNode(CameraDataNode(vertexNode.position))

                nodes.addAll(alphaNodes)

                val outputNode = addNode(OutputNode(depthCameraDataNode.clipSpacePosition, baseColorValue))
                outputNode.alphaCutoff = alphaCutoff
                outputNode.alphaMode = material.alphaMode
                outputNode.cullFaceMode = cullFaceMode
            }
        }

        val solidColor = gltf.conf.solidColor
        if (solidColor != null) {
            material.shaderChannels[ShaderChannel.SolidColor] = Shader(version = gltf.conf.shaderVersion).apply {
                addNode(vertexNode)

                val depthCameraDataNode = addNode(CameraDataNode(vertexNode.position))

                nodes.addAll(alphaNodes)

                val color = GLSLVec3Inline(solidColor.x, solidColor.y, solidColor.z)
                val alphaCombineOp = addNode(Op2Node(color, baseColorValue, "vec4(in1, in2.a)", GLSLType.Vec4))
                alphaCombineOp.isFragment = true
                alphaCombineOp.isVarying = false

                val outputNode = addNode(OutputNode(depthCameraDataNode.clipSpacePosition, alphaCombineOp.result))
                outputNode.alphaCutoff = alphaCutoff
                outputNode.alphaMode = material.alphaMode
                outputNode.cullFaceMode = cullFaceMode
            }
        }

        json.get("occlusionTexture") {
            float("strength") { occlusionStrength = it }

            int("texCoord") { occlusionTextureUV = it }
            val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$occlusionTextureUV", uvNodes)

            occlusionTexture = int("index")
            val textureNode = shader.addNode(TextureNode(uvNode.uv, (gltf.textures[occlusionTexture] as GLTFTexture).texture, false))
            pbrNode.occlusion = textureNode.color
            //float("strength", 1f)
        }

        if (json.contains("emissiveTexture") || json.contains("emissiveFactor")) {
            var emissiveValue: IShaderData = GLSL.zeroFloat

            json.get("emissiveTexture") {
                int("texCoord") { emissiveTextureUV = it }
                val uvNode = getOrCreateUVNode(shader, "TEXCOORD_$emissiveTextureUV", uvNodes)

                emissiveTexture = int("index")
                val textureNode = shader.addNode(TextureNode(uvNode.uv, (gltf.textures[emissiveTexture] as GLTFTexture).texture))
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
                    val op = Op2Node(emissiveValue, factor, "in1.xyz * in2", GLSLType.Vec3)
                    op.isFragment = true
                    op.isVarying = false
                    val node = shader.addNode(op)
                    node.result
                } else {
                    factor
                }
            }

            pbrNode.emissive = emissiveValue
        }

        gltf.conf.pbrConf(pbrNode)

        gltf.conf.configureMaterials(this)

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        if (name.isNotEmpty()) json["name"] = name
        if (alphaMode != "OPAQUE") json["alphaMode"] = alphaMode
        if (alphaCutoff != 0.5f) json["alphaCutoff"] = alphaCutoff
        if (doubleSided) json["doubleSided"] = doubleSided

        if (normalTexture != -1) {
            json.setObj("normalTexture") {
                set("index", normalTexture)
                if (normalTextureUV != 0) set("texCoord", normalTextureUV)
                if (normalScale != 1f) set("scale", normalScale)
            }
        }

        json.setObj("pbrMetallicRoughness") {
            if (baseColorTexture != -1) setObj("baseColorTexture") {
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
                setObj("metallicRoughnessTexture") {
                    set("index", metallicRoughnessTexture)
                    if (metallicRoughnessTextureUV != 0) set("texCoord", metallicRoughnessTextureUV)
                }
            }
            set("roughnessFactor", roughness)
            set("metallicFactor", metallic)
        }

        if (occlusionTexture != -1) {
            json.setObj("occlusionTexture") {
                set("index", occlusionTexture)
                if (occlusionTextureUV != 0) set("texCoord", occlusionTextureUV)
                if (occlusionStrength != 1f) set("strength", occlusionStrength)
            }
        }

        if (emissiveTexture != -1) {
            json.setObj("emissiveTexture") {
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

    override fun destroy() {
        material.destroy()
    }
}
