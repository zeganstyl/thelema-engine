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
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.json.IJsonObject

/**
 * @param vertPosition screen space vertex position (clip space)
 * @author zeganstyl
 * */
class OutputNode(
    /** Clip space vertex position */
    vertPosition: IShaderData = GLSL.zeroFloat,
    fragColor: IShaderData = GLSL.oneFloat
): ShaderNode() {
    constructor(block: OutputNode.() -> Unit): this() { block(this) }

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

    var alphaMode: String = Blending.OPAQUE
    var alphaCutoff: Float = 0.5f
    var cullFaceMode: Int = GL_BACK

    /** Fragments, which depth is close to camera's far clipping plane,
     * will fade out with transparency transition to zero.
     * Possible values are ranged from 0.0 to 1.0.
     * If it is not in range from 0.0 to 1.0, no transition will be applied. */
    var fadeStart = 0.8f
    private var cameraFarCached = -1f

    init {
        setInput(VertPosition, vertPosition)
        setInput(FragColor, fragColor)
    }

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

    override fun prepareToDrawMesh(mesh: IMesh) {
        super.prepareToDrawMesh(mesh)

        if (fadeStart in 0f..1f) {
            if (cameraFarCached != ActiveCamera.far) {
                shader["fadeStart"] = ActiveCamera.far * fadeStart
                shader["fadeMul"] = 1f / (ActiveCamera.far * (1f - fadeStart))
                cameraFarCached = ActiveCamera.far
            }
        }

        GL.isBlendingEnabled = alphaMode == Blending.BLEND || fadeStart in 0f..1f
        if (GL.isBlendingEnabled) GL.setSimpleAlphaBlending()
        GL.isCullFaceEnabled = GL.cullFaceMode == 0
        if (GL.isCullFaceEnabled) GL.cullFaceMode = cullFaceMode
    }

    override fun declarationVert(out: StringBuilder) {
        if (fadeStart in 0f..1f) {
            out.append("varying float depthForFade;")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (fadeStart in 0f..1f) {
            out.append("uniform float fadeStart;")
            out.append("uniform float fadeMul;")
            out.append("varying float depthForFade;")
        }
    }

    override fun executionVert(out: StringBuilder) {
        out.append("gl_Position = ${vertPosition.asVec4()};")

        if (fadeStart in 0f..1f) {
            out.append("depthForFade = gl_Position.w;\n")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("gl_FragColor = ${fragColor.asVec4()};\n")

        if (alphaMode == Blending.MASK) {
            out.append("if (gl_FragColor.a < 0.001 || gl_FragColor.a < $alphaCutoff) { discard; }\n")
            out.append("gl_FragColor.a = 1.0;\n")
        }

        if (fadeStart in 0f..1f) {
            out.append("gl_FragColor.a *= clamp(1.0 - (depthForFade - fadeStart) * fadeMul, 0.0, 1.0);\n")
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