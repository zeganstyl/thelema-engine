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

import app.thelema.g3d.Blending
import app.thelema.g3d.IUniformArgs
import app.thelema.gl.*
import app.thelema.math.TransformDataType
import app.thelema.shader.Shader

/** @author zeganstyl */
class VelocityOutputNode: ShaderNode(), IRootShaderNode {
    /** Use [TransformDataType] */
    var worldTransformType: String = TransformDataType.TRS

    override val componentName: String
        get() = "VelocityOutputNode"

    var aPositionName = "POSITION"
    var aBonesName = "JOINTS_"
    var aBoneWeightsName = "WEIGHTS_"

    var alphaMode: String = Blending.OPAQUE
    var alphaCutoff: Float = 0.5f
    var cullFaceMode: Int = GL_BACK

    /** Current world space position. It can be taken from [VertexNode.position] */
    var vertPosition by input(GLSLNode.vertex.position)

    /** Vertex shader normal, used for stretching */
    var normal by input(GLSLNode.vertex.normal)

    var alpha by input(GLSL.oneFloat)

    val uid: Int
        get() = 0

    override var proxy: IShaderNode?
        get() = this
        set(_) {}

    override fun bind(uniforms: IUniformArgs) {
        super.bind(uniforms)

        GL.isBlendingEnabled = false

        val cullFace = cullFaceMode != 0
        GL.isCullFaceEnabled = cullFace
        if (cullFace) GL.cullFaceMode = cullFaceMode
    }

    private fun boneInfluenceCode(component: String, bonesName: String, weightsName: String, sumName: String = "skinning"): String {
        return "    if ($weightsName.$component > 0.0) $sumName += $weightsName.$component * ${ArmatureUniforms.PrevBoneMatrices.uniformName}[int($bonesName.$component)];\n"
    }

    private fun skinningSetCode(out: StringBuilder, bonesName: String, weightsName: String, sumName: String = "skinning") {
        out.append(boneInfluenceCode("x", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("y", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("z", bonesName, weightsName, sumName))
        out.append(boneInfluenceCode("w", bonesName, weightsName, sumName))
    }

    private fun worldTransformPos(out: StringBuilder, prevPos: String) {
        when (worldTransformType) {
            TransformDataType.TRS -> out.append("$prevPos = ${MeshUniforms.PrevWorldMatrix.uniformName} * $prevPos;\n")
            TransformDataType.Translation -> out.append("$prevPos.xyz = $prevPos.xyz + uTransVec4$uid.xyz;\n")
            TransformDataType.TranslationScale -> out.append("$prevPos.xyz = $prevPos.xyz * uTransVec4$uid.w + uTransVec4$uid.xyz;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        val uid = 0

        val prevPos = "prevPos$uid"
        out.append("vec4 $prevPos = vec4($aPositionName, 1.0);\n")

        val prevSkinningName = "prevSkinning"

        out.append("if (${MeshUniforms.BonesNum.uniformName} > 0) {\n")
        val aBonesName = aBonesName
        val aBoneWeightsName = aBoneWeightsName
        for (i in 0 until VertexNode.bonesSetsNum) {
            val index = i.toString()
            skinningSetCode(out, "$aBonesName$index", "$aBoneWeightsName$index", prevSkinningName)
        }

        out.append("    $prevPos = $prevSkinningName * $prevPos;\n")
        out.append("} else {\n")
        worldTransformPos(out, prevPos)
        out.append("}\n")

        val prevPositionRef = "prevPosition"
        out.append("vec3 $prevPositionRef = $prevPos.xyz;\n")

        out.append("prevClipSpacePos = ${SceneUniforms.PrevViewProjMatrix.uniformName} * $prevPos;\n")

        out.append("${Shader.CLIP_SPACE_POS} = ${SceneUniforms.ViewProjMatrix.uniformName} * ${vertPosition.asVec4()};\n")

        // https://www.nvidia.com/docs/IO/8230/GDC2003_OpenGLShaderTricks.pdf
        val posRef = vertPosition.asVec3()
        out.append("""
if (dot($posRef - $prevPositionRef, ${normal.asVec3()}) > 0.0) {
    gl_Position = ${Shader.CLIP_SPACE_POS};
} else {
    gl_Position = prevClipSpacePos;
}
""")
    }

    override fun executionFrag(out: StringBuilder) {
        super.executionFrag(out)

        // convert clip space positions to NDC
        out.append("vec2 ndcPos = (${Shader.CLIP_SPACE_POS} / ${Shader.CLIP_SPACE_POS}.w).xy;\n")
        out.append("vec2 prevNdcPos = (prevClipSpacePos / prevClipSpacePos.w).xy;\n")
        out.append("FragColor = vec4(ndcPos - prevNdcPos, 0.0, ${alpha.asFloat()});\n")

        if (alphaMode == Blending.MASK) {
            out.append("\nif (FragColor.a < 0.001 || FragColor.a < $alphaCutoff) { discard; }")
            out.append("\nFragColor.a = 1.0;")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("in vec4 prevClipSpacePos;\n")
        out.append("out vec4 FragColor;\n")
    }

    override fun declarationVert(out: StringBuilder) {
        out.append("out vec4 prevClipSpacePos;\n")

        val uid = 0
        when (worldTransformType) {
            TransformDataType.Translation -> out.append("uniform vec4 uTransVec3$uid;\n")
            TransformDataType.TranslationScale -> out.append("uniform vec4 uTransVec4$uid;\n")
        }

        out.append("mat4 prevSkinning = mat4(0.0);\n")
    }
}