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

package app.thelema.shader.node

import app.thelema.gl.ArmatureUniforms
import app.thelema.gl.MeshUniforms
import app.thelema.gl.Vertex
import app.thelema.math.TransformDataType
import app.thelema.shader.Shader
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
class VertexNode: ShaderNode() {
    override val componentName: String
        get() = "VertexNode"

    private val positionName: String
        get() = Vertex.POSITION.name

    private val normalName: String
        get() = Vertex.NORMAL.name

    private val tangentName: String
        get() = Vertex.TANGENT.name

    /** Use [TransformDataType] */
    // TODO remove it, and make uniform to know if world matrix set or not
    @Deprecated("")
    var worldTransformType: String = TransformDataType.TRS

    // TODO add instance position attribute

    val position = output(GLSLVec3("position"))
    val normal = output(GLSLVec3("normal"))

    /** Tangent, bitangent, normal matrix */
    val tbn = output(GLSLMat3("tbn"))

    private val uWorldMatrix: String
        get() = MeshUniforms.WorldMatrix.uniformName

    val uid: Int
        get() = 0

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "    if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * BoneMatrices[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    private fun mat4ToMat3(mat4: String): String {
        return "mat3($mat4[0].xyz, $mat4[1].xyz, $mat4[2].xyz);"
    }

    private fun worldTransformPos(out: StringBuilder, pos: String) {
        when (worldTransformType) {
            TransformDataType.TRS -> out.append("    $pos = $uWorldMatrix * $pos;\n")
            TransformDataType.Translation -> out.append("    $pos.xyz = $pos.xyz + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationScale -> out.append("    $pos.xyz = $pos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (position.isUsed) {
            val pos = "pos$uid"
            out.append("vec4 $pos = vec4($positionName, 1.0);\n")

            val skinningName = "skinning"

            if (bonesSetsNum > 0) {
                out.append("if (${MeshUniforms.BonesNum.uniformName} > 0) {\n")
                val aBonesName = "JOINTS_"
                val aBoneWeightsName = "WEIGHTS_"
                for (i in 0 until bonesSetsNum) {
                    //val index = if (i == 0) "" else "$i"
                    val index = i.toString()
                    skinningSetCode(out, "$aBonesName$index", "$aBoneWeightsName$index", skinningName)
                }

                out.append("    $pos = $skinningName * $pos;\n")
                out.append("} else {\n")
                worldTransformPos(out, pos)
                out.append("}\n")
            } else {
                worldTransformPos(out, pos)
            }

            out.append("${position.ref} = $pos.xyz;\n")

            if (normal.isUsed || tbn.isUsed) {
                val aTangentName = tangentName
                val aNormalName = normalName
                val normalRef = normal.ref
                val tbnRef = tbn.ref

                out.append("if (${MeshUniforms.BonesNum.uniformName} > 0) {\n")
                //out.append("${normal.name} = normalize((transpose(inverse(skinning)) * vec4($attributeNormalName, 1.0)).xyz);\n")
                out.append("mat3 normalMat = ${mat4ToMat3(skinningName)};\n")
                out.append("$normalRef = normalize(normalMat * $aNormalName);\n")
                if (tbn.isUsed) {
                    out.append("vec3 T = normalize(normalMat * $aTangentName.xyz);\n")
                    out.append("vec3 B = cross(${normalRef}, T) * $aTangentName.w;\n")
                    out.append("$tbnRef = mat3(T, B, ${normalRef});\n")
                }
                out.append("} else {\n")
                if (worldTransformType == TransformDataType.TRS) {
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
                out.append("}\n")
            }

            out.append("${Shader.WORLD_SPACE_POS} = $pos;\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (position.isUsed) out.append("in vec3 ${position.ref};\n")
        if (normal.isUsed) out.append("in vec3 ${normal.ref};\n")
        if (tbn.isUsed) out.append("in mat3 ${tbn.ref};\n")
    }

    override fun declarationVert(out: StringBuilder) {
        if (position.isUsed) {
            out.append("in vec3 $positionName;\n")
            out.append("out vec3 ${position.ref};\n")

            when (worldTransformType) {
                TransformDataType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
                TransformDataType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
            }

            if (bonesSetsNum > 0) {
                out.append("#uniforms ${ArmatureUniforms.name}\n")
                out.append("mat4 skinning = mat4(0.0);\n")
            }

            for (i in 0 until bonesSetsNum) {
                out.append("in vec4 JOINTS_$i;\n")
                out.append("in vec4 WEIGHTS_$i;\n")
            }

            if (normal.isUsed || tbn.isUsed) {
                out.append("in vec3 $normalName;\n")
                out.append(normal.outRefEnd)
                out.append("uniform mat3 uNormalMatrix;\n")

                if (tbn.isUsed) {
                    out.append("in vec4 $tangentName;\n")
                    out.append(tbn.outRefEnd)
                }
            }
        }
    }

    @ThreadLocal
    companion object {
        var bonesSetsNum: Int = 1
    }
}