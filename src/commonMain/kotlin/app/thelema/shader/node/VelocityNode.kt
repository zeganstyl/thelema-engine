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

import app.thelema.g3d.IScene
import app.thelema.gl.IMesh
import app.thelema.math.TransformDataType
import app.thelema.math.MATH

/** @author zeganstyl */
class VelocityNode(): ShaderNode() {
    constructor(block: VelocityNode.() -> Unit): this() {
        block(this)
    }

    var maxBones: Int = 0

    /** Use [TransformDataType] */
    var worldTransformType: String = TransformDataType.TRS

    var bonesSetsNum: Int = 1

    override val componentName: String
        get() = "VelocityNode"

    var aPositionName = "POSITION"
    var aBonesName = "JOINTS_"
    var aBoneWeightsName = "WEIGHTS_"

    val prevPosition = output(GLSLVec3("prevPosition"))
    val velocity = output(GLSLVec2("velocity"))
    val stretchedClipSpacePosition = output(GLSLVec4("stretchedPosition"))

    /** Current world space position. It can be taken from [VertexNode.position] */
    var worldSpacePosition by input(GLSLNode.vertex.position)

    /** Current clip space position. It can be taken from [CameraDataNode.clipSpacePosition] */
    var clipSpacePosition by input(GLSLNode.camera.clipSpacePosition)

    /** Previous clip space position. It can be taken from [CameraDataNode.previousViewProjectionMatrix] */
    var previousViewProjectionMatrix by input(GLSLNode.camera.previousViewProjectionMatrix)

    /** Vertex shader normal, used for stretching */
    var normal by input(GLSLNode.vertex.normal)

    var viewProjectionMatrix by input(GLSLNode.camera.viewProjectionMatrix)

    private val uPrevBoneMatricesName: String
        get() = "uPrevBoneMatrices$uid"

    private val uPrevWorldMatrixName: String
        get() = "uPrevWorldMatrix$uid"

    private val uUsePrevBones: String
        get() = "uUsePrevBones$uid"

    val uid: Int
        get() = 0

    private val hasBones
        get() = maxBones > 0 && bonesSetsNum > 0

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "    if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * $uPrevBoneMatricesName[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        super.prepareShaderNode(mesh, scene)

        val worldMat = (mesh.previousWorldMatrix ?: mesh.worldMatrix) ?: MATH.IdentityMat4
        shader[uPrevWorldMatrixName] = worldMat
        shader[uUsePrevBones] = mesh.armature?.previousBoneMatrices?.isNotEmpty() == true

        val armature = mesh.armature
        if (armature != null) {
            val prevMatrices = armature.previousBoneMatrices
            shader.setMatrix4(uPrevBoneMatricesName, prevMatrices, length = prevMatrices.size)
        }
    }

    private fun worldTransformPos(out: StringBuilder, prevPos: String) {
        when (worldTransformType) {
            TransformDataType.TRS -> out.append("$prevPos = $uPrevWorldMatrixName * $prevPos;\n")
            TransformDataType.Translation -> out.append("$prevPos.xyz = $prevPos.xyz + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationScale -> out.append("$prevPos.xyz = $prevPos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationRotationY -> out.append("$prevPos.xyz = rotate_vertex_position(uTransVec4$uid.xyz, uTransVec4$uid.w) + uTransVec4$uid.xyz;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (prevPosition.isUsed || velocity.isUsed || stretchedClipSpacePosition.isUsed) {
            val uid = 0

            val prevPos = "prevPos$uid"
            out.append("vec4 $prevPos = vec4($aPositionName, 1.0);\n")

            val prevSkinningName = "prevSkinning"

            if (hasBones) {
                out.append("if ($uUsePrevBones) {\n")
                val aBonesName = aBonesName
                val aBoneWeightsName = aBoneWeightsName
                for (i in 0 until bonesSetsNum) {
                    val index = i.toString()
                    skinningSetCode(out, "$aBonesName$index", "$aBoneWeightsName$index", prevSkinningName)
                }

                out.append("    $prevPos = $prevSkinningName * $prevPos;\n")
                out.append("} else {\n")
                worldTransformPos(out, prevPos)
                out.append("}\n")
            } else {
                worldTransformPos(out, prevPos)
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
                TransformDataType.TRS -> out.append("uniform mat4 $uPrevWorldMatrixName;\n")
                TransformDataType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
                TransformDataType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
            }

            if (hasBones) {
                out.append("uniform mat4 $uPrevBoneMatricesName[$maxBones];\n")
                out.append("uniform bool $uUsePrevBones;\n")
                out.append("mat4 prevSkinning = mat4(0.0);\n")
            }
        }
    }
}