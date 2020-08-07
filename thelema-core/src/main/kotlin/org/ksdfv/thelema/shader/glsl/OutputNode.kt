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

import org.ksdfv.thelema.g3d.Blending
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_BACK
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.mesh.IMesh

/**
 * @param vertPosition screen space vertex position (clip space)
 * @author zeganstyl
 * */
class OutputNode(
    /** Clip space vertex position */
    vertPosition: IShaderData = GLSL.zeroFloat,
    fragColor: IShaderData = GLSL.oneFloat
): ShaderNode() {
    override val name: String
        get() = "Output"

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

    var alphaMode: Int = Blending.Opaque
    var alphaCutoff: Float = 0.5f
    var cullFaceMode: Int = GL_BACK

    init {
        setInput(VertPosition, vertPosition)
        setInput(FragColor, fragColor)
    }

    override fun read(json: IJsonObject) {
        super.read(json)

        alphaMode = Blending.getByName(json.string("alphaMode", "Opaque"))
        alphaCutoff = json.float("alphaCutoff", 0.5f)
        cullFaceMode = json.int("cullFaceMode", GL_BACK)
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        if (alphaMode != Blending.Opaque) json["alphaMode"] = Blending.getById(alphaMode)
        if (alphaCutoff != 0.5f) json["alphaCutoff"] = alphaCutoff
        if (cullFaceMode != GL_BACK) json["cullFaceMode"] = cullFaceMode
    }

    override fun prepareToDrawMesh(mesh: IMesh) {
        super.prepareToDrawMesh(mesh)

        GL.isBlendEnabled = alphaMode == Blending.Blend
        if (GL.isBlendEnabled) GL.setSimpleAlphaBlending()
        GL.isCullFaceEnabled = GL.cullFaceMode == 0
        if (GL.isCullFaceEnabled) GL.cullFaceMode = cullFaceMode
    }

    override fun executionVert(out: StringBuilder) {
        out.append("gl_Position = ${vertPosition.asVec4()};")
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("gl_FragColor = ${fragColor.asVec4()};\n")

        if (alphaMode == Blending.Clip) {
            out.append("if (gl_FragColor.a < 0.001 || gl_FragColor.a < $alphaCutoff) { discard; }\n")
            out.append("gl_FragColor.a = 1.0;\n")
        }
    }

    companion object {
        const val ClassId = "output"

        const val VertPosition = "vertPosition"
        const val FragColor = "fragColor"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(VertPosition, GLSLType.Vec4)
            put(FragColor, GLSLType.Vec4)
        }
    }
}