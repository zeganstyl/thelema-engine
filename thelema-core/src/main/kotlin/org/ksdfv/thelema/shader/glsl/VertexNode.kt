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

package org.ksdfv.thelema.shader.glsl

import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.node.TransformNodeType
import org.ksdfv.thelema.json.IJsonObject

/** For static, moving, skinned objects
 * @author zeganstyl */
class VertexNode(
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
    var aNormalName = "NORMAL"
    var aTangentName = "TANGENT"
    var aBonesName = "JOINTS_"
    var aBoneWeightsName = "WEIGHTS_"

    val position = defOut(GLSLVec3("position"))
    val normal = defOut(GLSLVec3("normal"))

    /** Tangent, bitangent, normal matrix */
    val tbn = defOut(GLSLMat3("tbn"))

    private var bonesTemp = FloatArray(maxBones * 16)

    private val uBoneMatricesName: String
        get() = "uBoneMatrices$uid"

    private val uWorldMatrix: String
        get() = "uWorldMatrix$uid"

    private val hasBones
        get() = maxBones > 0 && bonesSetsNum > 0

    val uid: Int
        get() = 0

    override fun read(json: IJsonObject) {
        super.read(json)

        aPositionName = json.string("aPositionName", "POSITION")
        aNormalName = json.string("aNormalName", "NORMAL")
        aTangentName = json.string("aTangentName", "TANGENT")
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
        if (aNormalName != "NORMAL") json["aNormalName"] = aNormalName
        if (aTangentName != "TANGENT") json["aTangentName"] = aTangentName
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

    fun set(other: VertexNode): VertexNode {
        maxBones = other.maxBones
        worldTransformType = other.worldTransformType
        bonesSetsNum = other.bonesSetsNum
        aPositionName = other.aPositionName
        aNormalName = other.aNormalName
        aTangentName = other.aTangentName
        aBonesName = other.aBonesName
        aBoneWeightsName = other.aBoneWeightsName
        return this
    }

    override fun shaderCompiled() {
        val bonesTempNewSize = maxBones * 16
        if (bonesTemp.size != bonesTempNewSize) bonesTemp = FloatArray(bonesTempNewSize)
    }

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * $uBoneMatricesName[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    override fun prepareObjectData(obj: IObject3D) {
        super.prepareObjectData(obj)

        shader[uWorldMatrix] = obj.worldMatrix

        val armature = obj.armature
        if (armature != null) {
            val matrices = armature.boneMatrices
            val bonesTemp = bonesTemp
            for (i in matrices.indices) {
                val floatOffset = i * 16
                val values = matrices[i].values
                for (j in 0 until 16) {
                    bonesTemp[floatOffset + j] = values[j]
                }
            }
            shader.setMatrix4(uBoneMatricesName, bonesTemp, length = matrices.size * 16)
        }
    }

    private fun mat4ToMat3(mat4: String): String {
        return "mat3($mat4[0].xyz, $mat4[1].xyz, $mat4[2].xyz);"
    }

    override fun executionVert(out: StringBuilder) {
        if (position.isUsed) {
            val pos = "pos$uid"
            out.append("vec4 $pos = vec4($aPositionName, 1.0);\n")

            val skinningName = "skinning"

            if (hasBones) {
                val aBonesName = aBonesName
                val aBoneWeightsName = aBoneWeightsName
                for (i in 0 until bonesSetsNum) {
                    //val index = if (i == 0) "" else "$i"
                    val index = i.toString()
                    skinningSetCode(out, "$aBonesName$index", "$aBoneWeightsName$index", skinningName)
                }

                out.append("$pos = $skinningName * $pos;\n")
            }

            when (worldTransformType) {
                TransformNodeType.TRS -> out.append("$pos = $uWorldMatrix * $pos;\n")
                TransformNodeType.Translation -> out.append("$pos.xyz = $pos.xyz + uTransVec4$uid.xyz;\n")
                TransformNodeType.TranslationScale -> out.append("$pos.xyz = $pos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
                TransformNodeType.TranslationRotationY -> out.append("$pos.xyz = rotate_vertex_position(uTransVec4$uid.xyz, uTransVec4$uid.w) + uTransVec4$uid.xyz;\n")
            }

            out.append("${position.ref} = $pos.xyz;\n")

            if (normal.isUsed || tbn.isUsed) {
                val aTangentName = aTangentName
                val aNormalName = aNormalName
                val normalRef = normal.ref
                val tbnRef = tbn.ref

                if (hasBones) {
                    //out.append("${normal.name} = normalize((transpose(inverse(skinning)) * vec4($attributeNormalName, 1.0)).xyz);\n")
                    out.append("mat3 normalMat = ${mat4ToMat3(skinningName)};\n")
                    out.append("$normalRef = normalize(normalMat * $aNormalName);\n")
                    if (tbn.isUsed) {
                        out.append("vec3 T = normalize(normalMat * $aTangentName.xyz);\n")
                        out.append("vec3 B = cross(${normalRef}, T) * $aTangentName.w;\n")
                        out.append("$tbnRef = mat3(T, B, ${normalRef});\n")
                    }
                } else {
                    if (worldTransformType == TransformNodeType.TRS) {
                        out.append("mat3 normalMat = ${mat4ToMat3(uWorldMatrix)};\n")
                        out.append("$normalRef = normalize(normalMat * $aNormalName);\n")
                        if (tbn.isUsed) {
                            out.append("vec3 T = normalize(normalMat * $aTangentName.xyz);\n")
                            out.append("vec3 B = cross(${normalRef}, T) * $aTangentName.w;\n")
                            out.append("$tbnRef = mat3(T, B, ${normalRef});\n")
                        }
                    } else {
                        out.append("$normalRef = $aNormalName;\n")
                        if (tbn.isUsed) {
                            out.append("vec3 T = $aTangentName.xyz;\n")
                            out.append("vec3 B = cross(${normalRef}, T) * $aTangentName.w;\n")
                            out.append("$tbnRef = mat3(T, B, ${normalRef});\n")
                        }
                    }
                }
            }
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (position.isUsed) out.append("$varIn vec3 ${position.ref};\n")
        if (normal.isUsed) out.append("$varIn vec3 ${normal.ref};\n")
        if (tbn.isUsed) out.append("$varIn mat3 ${tbn.ref};\n")
    }

    override fun declarationVert(out: StringBuilder) {
        if (position.isUsed) {
            out.append("$attribute vec3 $aPositionName;\n")
            out.append("$varOut vec3 ${position.ref};\n")

            when (worldTransformType) {
                TransformNodeType.TRS -> out.append("uniform mat4 $uWorldMatrix;\n")
                TransformNodeType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
                TransformNodeType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
                TransformNodeType.TranslationRotationY -> out.append("""
                    uniform vec4 uTransVec4$uid;

                    // https://www.geeks3d.com/20141201/how-to-rotate-a-vertex-by-a-quaternion-in-glsl/
                    vec3 rotate_vertex_position(vec3 position, float angle) {
                        float half_angle = angle * 0.5;
                        vec4 q = vec4(0.0, sin(half_angle), 0.0, cos(half_angle));
                        vec3 v = position.xyz;
                        return v + 2.0 * cross(q.xyz, cross(q.xyz, v) + q.w * v);
                    }
                """)
            }

            if (hasBones) {
                out.append("uniform mat4 $uBoneMatricesName[$maxBones];\n")
                out.append("mat4 skinning = mat4(0.0);\n")

                for (i in 0 until bonesSetsNum) {
                    out.append("$attribute vec4 ${aBonesName}$i;\n")
                    out.append("$attribute vec4 ${aBoneWeightsName}$i;\n")
                }
            }

            if (normal.isUsed || tbn.isUsed) {
                out.append("$attribute vec3 $aNormalName;\n")
                out.append("$varOut vec3 ${normal.ref};\n")
                out.append("uniform mat3 uNormalMatrix;\n")

                if (tbn.isUsed) {
                    out.append("$attribute vec4 $aTangentName;\n")
                    out.append("$varOut mat3 ${tbn.ref};\n")
                }
            }
        }
    }

    companion object {
        const val ClassId = "vertex"

        val InputForm = HashMap<String, Int>()
    }
}