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
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.IMesh
import app.thelema.json.IJsonObject

/** Vertex node for skyboxes. Supports velocity.
 *
 * @author zeganstyl */
class SkyboxVertexNode(
    viewProjectionMatrix: IShaderData = GLSL.zeroFloat
): ShaderNode() {
    constructor(block: SkyboxVertexNode.() -> Unit): this() { block(this) }

    override val name: String
        get() = "Skybox Vertex"

    var positionsName = "POSITION"

    var viewProjectionMatrix
        get() = input["viewProjectionMatrix"] ?: GLSL.oneFloat
        set(value) = setInput("viewProjectionMatrix", value)

    var previousViewProjectionMatrix
        get() = input["previousViewProjectionMatrix"] ?: GLSL.oneFloat
        set(value) = setInput("previousViewProjectionMatrix", value)

    val textureCoordinates = defOut(GLSLVec3("textureCoordinates"))

    val worldSpacePosition = defOut(GLSLVec3("worldSpacePosition"))

    val clipSpacePosition = defOut(GLSLVec4("clipSpacePosition"))

    /** For velocity rendering */
    val velocity = defOut(GLSLVec2("velocity"))

    val uSkyboxVertexTransformName
        get() = "uSkyboxVertexTransform"

    init {
        setInput("viewProjectionMatrix", viewProjectionMatrix)
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        positionsName = json.string("aPositionName", "POSITION")
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        if (positionsName != "POSITION") json["aPositionName"] = positionsName
    }

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        super.prepareShaderNode(mesh, scene)

        val pos = ActiveCamera.position
        shader.depthMask = false
        shader.set(uSkyboxVertexTransformName, pos.x, pos.y, pos.z, ActiveCamera.far)
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("$varOut vec3 vPosition;\n")
        out.append("${textureCoordinates.typedRef};\n")
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
        out.append("$attribute vec3 $positionsName;\n")
        out.append("$varOut vec3 vPosition;\n")
        out.append("$varOut ${textureCoordinates.typedRef};\n")
        out.append("$varOut ${worldSpacePosition.typedRef};\n")
        out.append("$varOut ${clipSpacePosition.typedRef};\n")
        out.append("$varOut vec4 prevClipSpacePos;\n")
        out.append("uniform vec4 $uSkyboxVertexTransformName;\n")
    }

    override fun executionFrag(out: StringBuilder) {
        super.executionFrag(out)

        out.append("${textureCoordinates.ref} = normalize(vPosition);\n")

        if (velocity.isUsed) {
            out.append("vec4 clipSpacePos = ${clipSpacePosition.asVec4()};\n")

            // convert clip space positions to NDC
            out.append("vec2 ndcPos = (clipSpacePos / clipSpacePos.w).xy;\n")
            out.append("vec2 prevNdcPos = (prevClipSpacePos / prevClipSpacePos.w).xy;\n")
            out.append("${velocity.ref} = ndcPos - prevNdcPos;\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        out.append("vPosition = ${positionsName}.xyz;\n")
        if (clipSpacePosition.isUsed || worldSpacePosition.isUsed || velocity.isUsed) {
            out.append("${worldSpacePosition.ref} = $uSkyboxVertexTransformName.xyz + ${positionsName}.xyz * $uSkyboxVertexTransformName.w;\n")

            if (clipSpacePosition.isUsed || velocity.isUsed) {
                out.append("${clipSpacePosition.ref} = ${viewProjectionMatrix.ref} * ${worldSpacePosition.asVec4()};\n")

                if (velocity.isUsed) {
                    out.append("prevClipSpacePos = ${previousViewProjectionMatrix.ref} * ${worldSpacePosition.asVec4()};\n")
                }
            }
        }
    }
}