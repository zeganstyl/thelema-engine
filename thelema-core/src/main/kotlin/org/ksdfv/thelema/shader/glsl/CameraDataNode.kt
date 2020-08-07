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

import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.IScene

/** @author zeganstyl */
class CameraDataNode(vertexPosition: IShaderData = GLSL.zeroFloat): ShaderNode() {
    override val name: String
        get() = "Camera Data"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    /** World space vertex position */
    var vertexPosition
        get() = input[VertexPosition] ?: GLSL.zeroFloat
        set(value) = setInput(VertexPosition, value)

    val cameraPosition = defOut(GLSLVec3("cameraPosition"))
    val viewProjectionMatrix = defOut(GLSLMat4("viewProjectionMatrix"))
    val previousViewProjectionMatrix = defOut(GLSLMat4("prevViewProjectionMatrix"))
    val viewMatrix = defOut(GLSLMat4("viewMatrix"))
    val projectionMatrix = defOut(GLSLMat4("projectionMatrix"))
    val inverseViewProjectionMatrix = defOut(GLSLMat4("inverseViewProjectionMatrix"))
    val normalizedViewVector = defOut(GLSLVec3("normalizedViewVector"))

    /** Non-normalized depth */
    val viewZDepth = defOut(GLSLFloat("viewZDepth"))

    /** Position after multiply Projection * View * vertex */
    val clipSpacePosition = defOut(GLSLVec4("clipSpacePosition"))

    /** Position after multiply View * vertex */
    val viewSpacePosition = defOut(GLSLVec4("viewSpacePosition"))

    init {
        setInput("vertexPosition", vertexPosition)
    }

    override fun prepareToDrawScene(scene: IScene) {
        super.prepareToDrawScene(scene)

        val cam = ActiveCamera
        shader[cameraPosition.ref] = cam.position
        shader[viewProjectionMatrix.ref] = cam.viewProjectionMatrix
        shader[previousViewProjectionMatrix.ref] = cam.previousViewProjectionMatrix
        shader[viewMatrix.ref] = cam.viewMatrix
        shader[projectionMatrix.ref] = cam.projectionMatrix
        shader[inverseViewProjectionMatrix.ref] = cam.inverseViewProjectionMatrix
    }

    override fun executionFrag(out: StringBuilder) {
        if (normalizedViewVector.isUsed) {
            out.append("${normalizedViewVector.ref} = normalize(${cameraPosition.asVec3()} - ${vertexPosition.asVec3()});\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("${clipSpacePosition.ref} = ${viewProjectionMatrix.ref} * ${vertexPosition.asVec4()};\n")
            if (viewZDepth.isUsed) {
                out.append("${viewZDepth.ref} = ${clipSpacePosition.ref}.z;\n")
            }
        }
        if (viewSpacePosition.isUsed) {
            out.append("${viewSpacePosition.ref} = ${viewMatrix.ref} * ${vertexPosition.asVec4()};\n")
        }
    }

    override fun declarationVert(out: StringBuilder) {
        if (cameraPosition.isUsed || normalizedViewVector.isUsed) {
            out.append("uniform ${cameraPosition.typedRef};\n")
        }
        if (clipSpacePosition.isUsed || viewProjectionMatrix.isUsed) {
            out.append("uniform ${viewProjectionMatrix.typedRef};\n")
        }
        if (previousViewProjectionMatrix.isUsed) {
            out.append("uniform ${previousViewProjectionMatrix.typedRef};\n")
        }
        if (viewSpacePosition.isUsed || viewMatrix.isUsed) out.append("uniform ${viewMatrix.typedRef};\n")
        if (projectionMatrix.isUsed) out.append("uniform ${projectionMatrix.typedRef};\n")
        if (inverseViewProjectionMatrix.isUsed) out.append("uniform ${inverseViewProjectionMatrix.typedRef};\n")
        if (clipSpacePosition.isUsed || viewZDepth.isUsed) {
            out.append("$varOut ${clipSpacePosition.typedRef};\n")
            if (viewZDepth.isUsed) {
                out.append("$varOut ${viewZDepth.typedRef};\n")
            }
        }
        if (viewSpacePosition.isUsed) out.append("$varOut ${viewSpacePosition.typedRef};\n")
    }

    override fun declarationFrag(out: StringBuilder) {
        if (cameraPosition.isUsed || normalizedViewVector.isUsed) out.append("uniform ${cameraPosition.typedRef};\n")
        if (normalizedViewVector.isUsed) out.append("vec3 ${normalizedViewVector.ref};\n")
        if (clipSpacePosition.isUsed) out.append("$varIn ${clipSpacePosition.typedRef};\n")
        if (viewSpacePosition.isUsed) out.append("$varIn ${viewSpacePosition.typedRef};\n")
        if (viewZDepth.isUsed) out.append("$varIn ${viewZDepth.typedRef};\n")
    }

    companion object {
        const val ClassId = "cameraData"

        const val VertexPosition = "vertexPosition"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(VertexPosition, GLSLType.Vec3)
        }
    }
}