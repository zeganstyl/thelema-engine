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

import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.json.IJsonObject

/** @author zeganstyl */
class SkyboxVertexNode(
    viewProjectionMatrix: IShaderData = GLSL.zeroFloat
): ShaderNode() {
    override val name: String
        get() = "Skybox Vertex"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var aPositionName = "POSITION"

    var viewProjectionMatrix
        get() = input[ViewProjectionMatrix] ?: GLSL.oneFloat
        set(value) = setInput(ViewProjectionMatrix, value)

    var previousViewProjectionMatrix
        get() = input[PreviousViewProjectionMatrix] ?: GLSL.oneFloat
        set(value) = setInput(PreviousViewProjectionMatrix, value)

    /** Use this as skybox texture coordinates */
    val attributePosition = defOut(GLSLVec3("attributePosition"))

    val worldSpacePosition = defOut(GLSLVec3("worldSpacePosition"))

    val clipSpacePosition = defOut(GLSLVec4("clipSpacePosition"))

    /** For velocity rendering */
    val velocity = defOut(GLSLVec2("velocity"))

    val uSkyboxVertexTransformName
        get() = "uSkyboxVertexTransform"

    init {
        setInput("viewProjectionMatrix", viewProjectionMatrix)
    }

    override fun read(json: IJsonObject) {
        super.read(json)

        aPositionName = json.string("aPositionName", "POSITION")
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        if (aPositionName != "POSITION") json["aPositionName"] = aPositionName
    }

    override fun prepareToDrawScene(scene: IScene) {
        super.prepareToDrawScene(scene)

        val pos = ActiveCamera.position
        shader.set(uSkyboxVertexTransformName, pos.x, pos.y, pos.z, ActiveCamera.far)
    }

    override fun declarationFrag(out: StringBuilder) {
        if (attributePosition.isUsed) out.append("$varIn ${attributePosition.typedRef};\n")
        if (worldSpacePosition.isUsed || clipSpacePosition.isUsed || velocity.isUsed) {
            out.append("$varIn ${worldSpacePosition.typedRef};\n")

            if (clipSpacePosition.isUsed || velocity.isUsed) {
                out.append("$varIn ${clipSpacePosition.typedRef};\n")

                if (velocity.isUsed) {
                    out.append("$varIn vec4 prevClipSpacePos;\n")
                    out.append("vec2 ${velocity.ref};\n")
                }
            }
        }
    }

    override fun declarationVert(out: StringBuilder) {
        out.append("$attribute vec3 $aPositionName;\n")
        out.append("$varOut ${attributePosition.typedRef};\n")
        out.append("$varOut ${worldSpacePosition.typedRef};\n")
        out.append("$varOut ${clipSpacePosition.typedRef};\n")
        out.append("$varOut vec4 prevClipSpacePos;\n")
        out.append("uniform vec4 $uSkyboxVertexTransformName;\n")
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

    override fun executionVert(out: StringBuilder) {
        if (clipSpacePosition.isUsed || worldSpacePosition.isUsed || velocity.isUsed) {
            out.append("${attributePosition.ref} = ${aPositionName}.xyz;\n")
            out.append("${worldSpacePosition.ref} = $uSkyboxVertexTransformName.xyz + ${aPositionName}.xyz * $uSkyboxVertexTransformName.w;\n")

            if (clipSpacePosition.isUsed || velocity.isUsed) {
                out.append("${clipSpacePosition.ref} = ${viewProjectionMatrix.ref} * ${worldSpacePosition.asVec4()};\n")

                if (velocity.isUsed) {
                    out.append("prevClipSpacePos = ${previousViewProjectionMatrix.ref} * ${worldSpacePosition.asVec4()};\n")
                }
            }
        }
    }

    companion object {
        const val ClassId = "skyboxVertex"

        const val ViewProjectionMatrix = "viewProjectionMatrix"
        const val PreviousViewProjectionMatrix = "previousViewProjectionMatrix"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(ViewProjectionMatrix, GLSLType.Mat4)
            put(PreviousViewProjectionMatrix, GLSLType.Mat4)
        }
    }
}