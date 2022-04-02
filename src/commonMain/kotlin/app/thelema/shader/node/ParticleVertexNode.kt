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

import app.thelema.gl.MeshUniforms
import app.thelema.gl.Vertex
import app.thelema.math.TransformDataType

/** @author zeganstyl */
class ParticleVertexNode(): ShaderNode() {
    override val componentName: String
        get() = "InstancedVertexNode"

    private val positionName: String
        get() = Vertex.POSITION.name

    private val normalName: String
        get() = Vertex.NORMAL.name

    private val tangentName: String
        get() = Vertex.TANGENT.name

    private val instancePositionName: String
        get() = Vertex.INSTANCE_POSITION.name

    val position = output(GLSLVec3("position"))
    val normal = output(GLSLVec3("normal"))

    /** Tangent, bitangent, normal matrix */
    val tbn = output(GLSLMat3("tbn"))

    private val uWorldMatrix: String
        get() = MeshUniforms.WorldMatrix.uniformName

    var worldTransformType = TransformDataType.None

    var alwaysRotateToCamera: Boolean = false

    val uid: Int
        get() = 0

    private fun mat4ToMat3(mat4: String): String {
        return "mat3($mat4[0].xyz, $mat4[1].xyz, $mat4[2].xyz);"
    }

    private fun worldTransformPos(out: StringBuilder, pos: String) {
        when (worldTransformType) {
            TransformDataType.TRS -> out.append("$pos = $uWorldMatrix * $pos;\n")
            TransformDataType.Translation -> out.append("$pos.xyz = $pos.xyz + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationScale -> out.append("$pos.xyz = $pos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (position.isUsed) {
            val pos = "pos$uid"
            out.append("vec4 $pos = vec4($positionName, 1.0);\n")

            worldTransformPos(out, pos)

            out.append("${position.ref} = $pos.xyz;\n")

            if (normal.isUsed || tbn.isUsed) {
                val aTangentName = tangentName
                val aNormalName = normalName
                val normalRef = normal.ref
                val tbnRef = tbn.ref

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

    override fun declarationFrag(out: StringBuilder) {
        if (position.isUsed) out.append(position.inRefEnd)
        if (normal.isUsed) out.append(normal.inRefEnd)
        if (tbn.isUsed) out.append(tbn.inRefEnd)
    }

    override fun declarationVert(out: StringBuilder) {
        if (position.isUsed) {
            out.append("in vec3 $positionName;\n")
            out.append(position.outRefEnd)

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
}