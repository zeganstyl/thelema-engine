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
import app.thelema.gl.GL
import app.thelema.gl.GL_BACK
import app.thelema.img.GBuffer
import app.thelema.json.IJsonObject
import app.thelema.gl.IMesh

/** For using with [GBuffer]
 * @author zeganstyl */
class GBufferOutputNode: ShaderNode() {
    override val name: String
        get() = "G-Buffer Output"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    /** Clip space vertex position */
    var vertPosition
        get() = input[VertPosition] ?: GLSL.zeroFloat
        set(value) = setInput(VertPosition, value)

    var fragColor
        get() = input[FragColor] ?: GLSL.oneFloat
        set(value) = setInput(FragColor, value)

    var fragNormal
        get() = input[FragNormal] ?: GLSL.defaultNormal
        set(value) = setInput(FragNormal, value)

    var fragPosition
        get() = input[FragPosition] ?: GLSL.zeroFloat
        set(value) = setInput(FragPosition, value)

    override val output: Map<String, IShaderData> = HashMap()

    var alphaMode: String = Blending.OPAQUE
    var alphaCutoff: Float = 0.5f
    var cullFaceMode: Int = GL_BACK

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        alphaMode = json.string("alphaMode", Blending.OPAQUE)
        alphaCutoff = json.float("alphaCutoff", 0.5f)
        cullFaceMode = json.int("cullFaceMode", GL_BACK)
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["alphaMode"] = alphaMode
        if (alphaCutoff != 0.5f) json["alphaCutoff"] = alphaCutoff
        json["cullFaceMode"] = cullFaceMode
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("layout (location = 0) out vec4 gColor;\n")
        out.append("layout (location = 1) out vec4 gNormal;\n")
        out.append("layout (location = 2) out vec4 gPosition;\n")
    }

    override fun prepareToDrawMesh(mesh: IMesh) {
        super.prepareToDrawMesh(mesh)

        GL.isBlendingEnabled = alphaMode == Blending.BLEND
        if (GL.isBlendingEnabled) GL.setSimpleAlphaBlending()
        GL.isCullFaceEnabled = GL.cullFaceMode == 0
        if (GL.isCullFaceEnabled) GL.cullFaceMode = cullFaceMode
    }

    override fun executionVert(out: StringBuilder) {
        out.append("gl_Position = ${vertPosition.asVec4()};\n")
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("gColor = ${fragColor.asVec4()};\n")
        out.append("gNormal = ${fragNormal.asVec4()};\n")
        out.append("gPosition = ${fragPosition.asVec4()};\n")

        if (alphaMode == Blending.MASK) {
            out.append("if (gColor.a < $alphaCutoff) { discard; }\n")
            out.append("gColor.a = 1.0;\n")
        }
    }

    companion object {
        const val ClassId = "gBufferOutput"

        const val VertPosition = "vertPosition"
        const val FragColor = "fragColor"
        const val FragNormal = "fragNormal"
        const val FragPosition = "fragPosition"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(VertPosition, GLSLType.Vec4)
            put(FragColor, GLSLType.Vec4)
            put(FragNormal, GLSLType.Vec4)
            put(FragPosition, GLSLType.Vec4)
        }
    }
}