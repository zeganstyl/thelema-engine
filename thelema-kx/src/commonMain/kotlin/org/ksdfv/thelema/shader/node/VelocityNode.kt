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

package org.ksdfv.thelema.shader.node

import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.node.TransformNodeType
import org.ksdfv.thelema.json.IJsonObject

/** @author zeganstyl */
class VelocityNode(
    worldSpacePosition: IShaderData = GLSL.zeroFloat,
    clipSpacePosition: IShaderData = GLSL.zeroFloat,
    previousViewProjectionMatrix: IShaderData = GLSL.zeroFloat,
    normal: IShaderData = GLSL.defaultNormal,

    var maxBones: Int = 0,

    /** Use [TransformNodeType] */
    var worldTransformType: Int = TransformNodeType.TRS,

    var bonesSetsNum: Int = 1
): ShaderNode() {
    override val name: String
        get() = "Vertex"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var aPositionName = "POSITION"
    var aBonesName = "JOINTS_"
    var aBoneWeightsName = "WEIGHTS_"

    val prevPosition = defOut(GLSLVec3("prevPosition"))
    val velocity = defOut(GLSLVec2("velocity"))
    val stretchedClipSpacePosition = defOut(GLSLVec4("stretchedPosition"))

    /** Current world space position. It can be taken from [VertexNode.position] */
    var worldSpacePosition: IShaderData
        get() = input[WorldSpacePosition] ?: GLSL.zeroFloat
        set(value) = setInput(WorldSpacePosition, value)

    /** Current clip space position. It can be taken from [CameraDataNode.clipSpacePosition] */
    var clipSpacePosition: IShaderData
        get() = input[ClipSpacePosition] ?: GLSL.zeroFloat
        set(value) = setInput(ClipSpacePosition, value)

    /** Previous clip space position. It can be taken from [CameraDataNode.previousViewProjectionMatrix] */
    var previousViewProjectionMatrix: IShaderData
        get() = input[PreviousViewProjectionMatrix] ?: GLSL.oneFloat
        set(value) = setInput(PreviousViewProjectionMatrix, value)

    /** Vertex shader normal, used for stretching */
    var normal: IShaderData
        get() = input[Normal] ?: GLSL.defaultNormal
        set(value) = setInput(Normal, value)

    var viewProjectionMatrix: IShaderData
        get() = input[ViewProjectionMatrix] ?: GLSL.oneFloat
        set(value) = setInput(ViewProjectionMatrix, value)

    private var bonesTemp = FloatArray(maxBones * 16)

    private val uPrevBoneMatricesName: String
        get() = "uPrevBoneMatrices$uid"

    private val uPrevWorldMatrixName: String
        get() = "uPrevWorldMatrix$uid"

    val uid: Int
        get() = 0

    private val hasBones
        get() = maxBones > 0 && bonesSetsNum > 0

    init {
        setInput("worldSpacePosition", worldSpacePosition)
        setInput("clipSpacePosition", clipSpacePosition)
        setInput("previousViewProjectionMatrix", previousViewProjectionMatrix)
        setInput("normal", normal)
    }

    override fun read(json: IJsonObject) {
        super.read(json)

        aPositionName = json.string("aPositionName", "POSITION")
        aBonesName = json.string("aBonesName", "JOINTS_")
        aBoneWeightsName = json.string("aBoneWeightsName", "WEIGHTS_")

        maxBones = json.int("maxBones", 0)
        bonesSetsNum = json.int("bonesSetsNum", 1)
        worldTransformType = when (json.string("worldTransformType")) {
            "trs" -> TransformNodeType.TRS
            "translation" -> TransformNodeType.Translation
            "translationScale" -> TransformNodeType.TranslationScale
            "translationRotation" -> TransformNodeType.TranslationRotation
            "translationRotationY" -> TransformNodeType.TranslationRotationY
            "translationScaleProportional" -> TransformNodeType.TranslationScaleProportional
            else -> TransformNodeType.None
        }
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        if (aPositionName != "POSITION") json["aPositionName"] = aPositionName
        if (aBonesName != "JOINTS_") json["aBonesName"] = aBonesName
        if (aBoneWeightsName != "WEIGHTS_") json["aBoneWeightsName"] = aBoneWeightsName

        if (maxBones > 0) json["maxBones"] = maxBones
        if (bonesSetsNum != 1) json["bonesSetsNum"] = bonesSetsNum
        if (worldTransformType != TransformNodeType.None) {
            json["worldTransformType"] = when (worldTransformType) {
                TransformNodeType.TRS -> "trs"
                TransformNodeType.Translation -> "translation"
                TransformNodeType.TranslationScale -> "translationScale"
                TransformNodeType.TranslationRotation -> "translationRotation"
                TransformNodeType.TranslationRotationY -> "translationRotationY"
                TransformNodeType.TranslationScaleProportional -> "translationScaleProportional"
                else -> "none"
            }
        }
    }

    override fun shaderCompiled() {
        val bonesTempNewSize = maxBones * 16
        if (bonesTemp.size != bonesTempNewSize) bonesTemp = FloatArray(bonesTempNewSize)
    }

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * $uPrevBoneMatricesName[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    override fun prepareObjectData(obj: IObject3D) {
        super.prepareObjectData(obj)

        shader[uPrevWorldMatrixName] = obj.previousTransform.worldMatrix

        val armature = obj.armature
        if (armature != null) {
            val bonesTemp = bonesTemp
            val prevMatrices = armature.previousBoneMatrices
            for (i in prevMatrices.indices) {
                val floatOffset = i * 16
                val values = prevMatrices[i].values
                for (j in 0 until 16) {
                    bonesTemp[floatOffset + j] = values[j]
                }
            }
            shader.setMatrix4(uPrevBoneMatricesName, bonesTemp, length = prevMatrices.size * 16)
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (prevPosition.isUsed || velocity.isUsed || stretchedClipSpacePosition.isUsed) {
            val uid = 0

            val prevPos = "prevPos$uid"
            out.append("vec4 $prevPos = vec4($aPositionName, 1.0);\n")

            val prevSkinningName = "prevSkinning"

            if (hasBones) {
                val aBonesName = aBonesName
                val aBoneWeightsName = aBoneWeightsName
                for (i in 0 until bonesSetsNum) {
                    val index = i.toString()
                    skinningSetCode(out, "$aBonesName$index", "$aBoneWeightsName$index", prevSkinningName)
                }

                out.append("$prevPos = $prevSkinningName * $prevPos;\n")
            }

            when (worldTransformType) {
                TransformNodeType.TRS -> out.append("$prevPos = $uPrevWorldMatrixName * $prevPos;\n")
                TransformNodeType.Translation -> out.append("$prevPos.xyz = $prevPos.xyz + uTransVec4$uid.xyz;\n")
                TransformNodeType.TranslationScale -> out.append("$prevPos.xyz = $prevPos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
                TransformNodeType.TranslationRotationY -> out.append("$prevPos.xyz = rotate_vertex_position(uTransVec4$uid.xyz, uTransVec4$uid.w) + uTransVec4$uid.xyz;\n")
            }

            val prevPositionRef = prevPosition.ref
            out.append("$prevPositionRef = $prevPos.xyz;\n")

            if (velocity.isUsed || stretchedClipSpacePosition.isUsed) {
                out.append("prevClipSpacePos = ${previousViewProjectionMatrix.ref} * $prevPos;\n")

                if (stretchedClipSpacePosition.isUsed) {
                    // https://www.nvidia.com/docs/IO/8230/GDC2003_OpenGLShaderTricks.pdf

                    val posRef = worldSpacePosition.asVec3()
                    val stretchedClipSpacePositionRef = stretchedClipSpacePosition.ref
                    out.append("""
if (dot($posRef - ${prevPosition.ref}, ${normal.asVec3()}) > 0.0) {
    $stretchedClipSpacePositionRef = ${clipSpacePosition.asVec4()};
} else {
    $stretchedClipSpacePositionRef = prevClipSpacePos;
}
""")
                }
            }
        }
    }

    override fun executionFrag(out: StringBuilder) {
        super.executionFrag(out)

        if (velocity.isUsed) {
            out.append("vec4 clipSpacePos = ${clipSpacePosition.asVec4()};\n")

            // convert clip space positions to NDC
            out.append("vec2 ndcPos = (clipSpacePos / clipSpacePos.w).xy;\n")
            out.append("vec2 prevNdcPos = (prevClipSpacePos / prevClipSpacePos.w).xy;\n")
            out.append("${velocity.ref} = ndcPos - prevNdcPos;\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (prevPosition.isUsed || velocity.isUsed) {
            out.append("$varIn vec3 ${prevPosition.ref};\n")
            out.append("$varIn vec4 prevClipSpacePos;\n")
            if (velocity.isUsed) out.append("vec2 ${velocity.ref};\n")
        }
    }

    override fun declarationVert(out: StringBuilder) {
        if (prevPosition.isUsed || velocity.isUsed || stretchedClipSpacePosition.isUsed) {
            out.append("$varOut vec3 ${prevPosition.ref};\n")

            if (velocity.isUsed || stretchedClipSpacePosition.isUsed) {
                out.append("$varOut vec4 prevClipSpacePos;\n")
                if (stretchedClipSpacePosition.isUsed) out.append("vec4 ${stretchedClipSpacePosition.ref};\n")
            }

            val uid = 0
            when (worldTransformType) {
                TransformNodeType.TRS -> out.append("uniform mat4 $uPrevWorldMatrixName;\n")
                TransformNodeType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
                TransformNodeType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
            }

            if (hasBones) {
                out.append("uniform mat4 $uPrevBoneMatricesName[$maxBones];\n")
                out.append("mat4 prevSkinning = mat4(0.0);\n")
            }
        }
    }

    companion object {
        const val ClassId = "velocity"

        const val WorldSpacePosition = "worldSpacePosition"
        const val ClipSpacePosition = "clipSpacePosition"
        const val PreviousViewProjectionMatrix = "previousViewProjectionMatrix"
        const val Normal = "normal"
        const val ViewProjectionMatrix = "viewProjectionMatrix"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(WorldSpacePosition, GLSLType.Vec3)
            put(ClipSpacePosition, GLSLType.Vec4)
            put(PreviousViewProjectionMatrix, GLSLType.Mat4)
            put(Normal, GLSLType.Vec3)
            put(ViewProjectionMatrix, GLSLType.Mat4)
        }
    }
}