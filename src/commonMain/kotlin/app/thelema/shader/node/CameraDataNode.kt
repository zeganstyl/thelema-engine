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

import app.thelema.g3d.IUniforms
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.Vertex
import app.thelema.math.Mat3

/** @author zeganstyl */
class CameraDataNode(vertexPosition: IShaderData = GLSLNode.vertex.position): ShaderNode() {
    override val componentName: String
        get() = "CameraDataNode"

    /** World space vertex position */
    var vertexPosition: IShaderData by input()

    val viewProjectionMatrix = GLSLMat4("viewProjectionMatrix")
    val rotateToCameraMatrix = GLSLMat3("rotateToCameraMatrix")
    @Deprecated("use your own uniform")
    val previousViewProjectionMatrix = output(GLSLMat4("prevViewProjectionMatrix"))
    val viewMatrix = GLSLMat4("viewMatrix")

    /** Non-normalized depth */
    val viewZDepth = output(GLSLFloat("viewZDepth"))

    /** Position after multiply Projection * View * vertex */
    val clipSpacePosition = output(GLSLVec4("clipSpacePosition"))

    /** Position after multiply View * vertex */
    val viewSpacePosition = output(GLSLVec4("viewSpacePosition"))

    val instancePositionName: String
        get() = Vertex.INSTANCE_POSITION.name

    var useInstancePosition = false
    var alwaysRotateObjectToCamera = false

    private val mat3Tmp by lazy { Mat3() }

    init {
        this.vertexPosition = vertexPosition
    }

    override fun bind(uniforms: IUniforms) {
        super.bind(uniforms)

        val cam = ActiveCamera
        shader[viewProjectionMatrix.ref] = cam.viewProjectionMatrix
        previousViewProjectionMatrix.ref.also { if (shader.hasUniform(it)) shader[it] = cam.previousViewProjectMatrix ?: cam.viewProjectionMatrix }
        viewMatrix.ref.also { if (shader.hasUniform(it)) shader[it] = cam.viewMatrix }

        if (alwaysRotateObjectToCamera) {
            mat3Tmp.set(ActiveCamera.viewMatrix)
            mat3Tmp.m10 = -mat3Tmp.m10
            mat3Tmp.m11 = -mat3Tmp.m11
            mat3Tmp.m12 = -mat3Tmp.m12
            shader.set(rotateToCameraMatrix.ref, mat3Tmp, true)
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("${clipSpacePosition.ref} = ${viewProjectionMatrix.ref} * ${getPositionRefVec4()};\n")
            if (viewZDepth.isUsed) {
                out.append("${viewZDepth.ref} = ${clipSpacePosition.ref}.z;\n")
            }
        }
        if (viewSpacePosition.isUsed) {
            out.append("${viewSpacePosition.ref} = ${viewMatrix.ref} * ${getPositionRefVec4()};\n")
        }
    }

    private fun getPositionRef(): String = if (useInstancePosition) {
        "${if (alwaysRotateObjectToCamera) "${rotateToCameraMatrix.ref} * " else ""}${vertexPosition.asVec3()} + $instancePositionName"
    } else {
        "${if (alwaysRotateObjectToCamera) "${rotateToCameraMatrix.ref} * " else ""}${vertexPosition.asVec3()}"
    }

    private fun getPositionRefVec4(): String = "vec4(${getPositionRef()}, 1.0)"

    override fun declarationVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || viewProjectionMatrix.isUsed) {
            out.append("uniform ${viewProjectionMatrix.typedRef};\n")
        }
        if (previousViewProjectionMatrix.isUsed) {
            out.append("uniform ${previousViewProjectionMatrix.typedRef};\n")
        }
        if (alwaysRotateObjectToCamera) {
            out.append("uniform ${rotateToCameraMatrix.typedRef};\n")
        }
        if (viewSpacePosition.isUsed || viewMatrix.isUsed) out.append("uniform ${viewMatrix.typedRef};\n")
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("out ${clipSpacePosition.typedRef};\n")
            if (viewZDepth.isUsed) {
                out.append("out ${viewZDepth.typedRef};\n")
            }
        }
        if (viewSpacePosition.isUsed) out.append("out ${viewSpacePosition.typedRef};\n")
        if (useInstancePosition) out.append("in vec3 $instancePositionName;\n")
    }

    override fun declarationFrag(out: StringBuilder) {
        if (clipSpacePosition.isUsed) out.append("in ${clipSpacePosition.typedRef};\n")
        if (viewSpacePosition.isUsed) out.append("in ${viewSpacePosition.typedRef};\n")
        if (viewZDepth.isUsed) out.append("in ${viewZDepth.typedRef};\n")
        if (viewMatrix.isUsed) out.append("uniform ${viewMatrix.typedRef};\n")
    }
}