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

import app.thelema.g3d.IUniformArgs
import app.thelema.gl.SceneUniforms

/** @author zeganstyl */
class CameraDataNode(vertexPosition: IShaderData = GLSLNode.vertex.position): ShaderNode() {
    override val componentName: String
        get() = "CameraDataNode"

    /** World space vertex position */
    var vertexPosition: IShaderData by input()

    /** Non-normalized depth */
    val viewZDepth = output(GLSLFloat("viewZDepth"))

    /** Position after multiply Projection * View * vertex */
    val clipSpacePosition = output(GLSLVec4("clipSpacePosition"))

    /** Position after multiply View * vertex */
    val viewSpacePosition = output(GLSLVec4("viewSpacePosition"))

    private val viewProjectionMatrixName: String
        get() = SceneUniforms.ViewProjMatrix.uniformName

    init {
        this.vertexPosition = vertexPosition
    }

    override fun bind(uniforms: IUniformArgs) {
        super.bind(uniforms)

//        if (alwaysRotateObjectToCamera) {
//            mat3Tmp.set(ActiveCamera.viewMatrix)
//            mat3Tmp.m10 = -mat3Tmp.m10
//            mat3Tmp.m11 = -mat3Tmp.m11
//            mat3Tmp.m12 = -mat3Tmp.m12
//            shader.set(rotateToCameraMatrix.ref, mat3Tmp, true)
//        }
    }

    override fun executionVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("${clipSpacePosition.ref} = $viewProjectionMatrixName * ${getPositionRefVec4()};\n")
            if (viewZDepth.isUsed) {
                out.append("${viewZDepth.ref} = ${clipSpacePosition.ref}.z;\n")
            }
        }
        if (viewSpacePosition.isUsed) {
            out.append("${viewSpacePosition.ref} = ${SceneUniforms.ViewProjMatrix.uniformName} * ${getPositionRefVec4()};\n")
        }
    }

    private fun getPositionRefVec4(): String = "vec4(${vertexPosition.asVec3()}, 1.0)"

    override fun declarationVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("out ${clipSpacePosition.typedRef};\n")
            if (viewZDepth.isUsed) {
                out.append("out ${viewZDepth.typedRef};\n")
            }
        }
        if (viewSpacePosition.isUsed) out.append("out ${viewSpacePosition.typedRef};\n")
    }

    override fun declarationFrag(out: StringBuilder) {
        if (clipSpacePosition.isUsed) out.append("in ${clipSpacePosition.typedRef};\n")
        if (viewSpacePosition.isUsed) out.append("in ${viewSpacePosition.typedRef};\n")
        if (viewZDepth.isUsed) out.append("in ${viewZDepth.typedRef};\n")
    }
}