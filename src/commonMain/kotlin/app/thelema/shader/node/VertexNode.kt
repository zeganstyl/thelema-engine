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

import app.thelema.g3d.IArmature
import app.thelema.g3d.IUniforms
import app.thelema.gl.Uniform
import app.thelema.gl.Vertex
import app.thelema.math.TransformDataType
import app.thelema.math.MATH

/** For static, moving, skinned objects
 * @author zeganstyl */
class VertexNode(
    var maxBones: Int = 0,

    /** Use [TransformDataType] */
    var worldTransformType: String = TransformDataType.TRS,

    var bonesSetsNum: Int = 1
): ShaderNode() {
    constructor(block: VertexNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "VertexNode"

    private val positionName: String
        get() = Vertex.POSITION.name

    private val normalName: String
        get() = Vertex.NORMAL.name

    private val tangentName: String
        get() = Vertex.TANGENT.name

    val position = output(GLSLVec3("position"))
    val normal = output(GLSLVec3("normal"))

    /** Tangent, bitangent, normal matrix */
    val tbn = output(GLSLMat3("tbn"))

    private val uBoneMatricesName: String
        get() = "uBoneMatrices$uid"

    private val uWorldMatrix: String
        get() = "uWorldMatrix$uid"

    private val uUseBones: String
        get() = "uUseBones$uid"

    private val hasBones
        get() = maxBones > 0 && bonesSetsNum > 0

    val uid: Int
        get() = 0

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "    if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * $uBoneMatricesName[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    override fun bind(uniforms: IUniforms) {
        super.bind(uniforms)

        shader[uWorldMatrix] = uniforms.worldMatrix ?: MATH.IdentityMat4

        val armature = uniforms.get<IArmature>(Uniform.Armature)
        shader[uUseBones] = armature?.boneMatrices?.isNotEmpty() == true

        if (armature != null) {
            val matrices = armature.boneMatrices
            if (matrices.isNotEmpty()) {
                shader.setMatrix4(uBoneMatricesName, matrices, 0, armature.bones.size)
            }
        }
    }

    private fun mat4ToMat3(mat4: String): String {
        return "mat3($mat4[0].xyz, $mat4[1].xyz, $mat4[2].xyz);"
    }

    private fun worldTransformPos(out: StringBuilder, pos: String) {
        when (worldTransformType) {
            TransformDataType.TRS -> out.append("$pos = $uWorldMatrix * $pos;\n")
            TransformDataType.Translation -> out.append("$pos.xyz = $pos.xyz + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationScale -> out.append("$pos.xyz = $pos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationRotationY -> out.append("$pos.xyz = rotate_vertex_position(uTransVec4$uid.xyz, uTransVec4$uid.w) + uTransVec4$uid.xyz;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (position.isUsed) {
            val pos = "pos$uid"
            out.append("vec4 $pos = vec4($positionName, 1.0);\n")

            val skinningName = "skinning"

            if (hasBones) {
                out.append("if ($uUseBones) {\n")
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
                }
            }
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
                TransformDataType.TRS -> out.append("uniform mat4 $uWorldMatrix;\n")
                TransformDataType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
                TransformDataType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
                TransformDataType.TranslationRotationY -> out.append("""
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
                out.append("uniform bool $uUseBones;\n")
                out.append("mat4 skinning = mat4(0.0);\n")

                for (i in 0 until bonesSetsNum) {
                    out.append("in vec4 JOINTS_$i;\n")
                    out.append("in vec4 WEIGHTS_$i;\n")
                }
            }

            if (normal.isUsed || tbn.isUsed) {
                out.append("in vec3 $normalName;\n")
                out.append("out vec3 ${normal.ref};\n")
                out.append("uniform mat3 uNormalMatrix;\n")

                if (tbn.isUsed) {
                    out.append("in vec4 $tangentName;\n")
                    out.append("out mat3 ${tbn.ref};\n")
                }
            }
        }
    }
}