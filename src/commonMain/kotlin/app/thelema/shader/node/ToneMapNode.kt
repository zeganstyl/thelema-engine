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
import app.thelema.json.IJsonObject

/** For tone mapping, implied that [TextureNode.sRGB] is enabled.
 *
 * Implemented from [Khronos glTF-Sample-Viewer](https://github.com/KhronosGroup/glTF-Sample-Viewer/blob/master/src/shaders/tonemapping.glsl)
 *
 * @author zeganstyl */
class ToneMapNode(
    inputColor: IShaderData = GLSL.oneFloat,
    var toneMapType: Int = LinearToSRGBToneMap
): ShaderNode() {
    override val name: String
        get() = "Tone Map"

    var inputColor
        get() = input[InputColor] ?: GLSL.oneFloat
        set(value) = setInput(InputColor, value)

    val result = defOut(GLSLVec4("toneMappedColor"))

    init {
        setInput(InputColor, inputColor)
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        toneMapType = when (json.string("toneMapType")) {
            "LinearToSRGB" -> LinearToSRGBToneMap
            "Uncharted" -> UnchartedToneMap
            "HejlRichard" -> HejlRichardToneMap
            "ACES" -> ACESToneMap
            else -> LinearToSRGBToneMap
        }
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["toneMapType"] = when (toneMapType) {
            LinearToSRGBToneMap -> "LinearToSRGB"
            UnchartedToneMap -> "Uncharted"
            HejlRichardToneMap -> "HejlRichard"
            ACESToneMap -> "ACES"
            else -> "LinearToSRGB"
        }
    }

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        super.prepareShaderNode(mesh, scene)

        shader["uExposure"] = scene?.world?.exposure ?: 1f
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("uniform float uExposure;\n")
        out.append("${result.typedRef} = vec4(0.0);\n")
        out.append("vec3 toneMap(vec3 color) {\n")
        out.append("color *= uExposure;\n")
        out.append(when (toneMapType) {
            UnchartedToneMap -> {
                out.append("const float A = 0.15;\n")
                out.append("const float B = 0.50;\n")
                out.append("const float C = 0.10;\n")
                out.append("const float D = 0.20;\n")
                out.append("const float E = 0.02;\n")
                out.append("const float F = 0.30;\n")
                out.append("color = color * 2.0;\n")
                out.append("color = ((color*(A*color+C*B)+D*E)/(color*(A*color+B)+D*F))-E/F;\n")
                out.append("return pow(color * $WhiteScale, vec3($InvGamma));\n")
            }
            HejlRichardToneMap -> {
                out.append("color = max(vec3(0.0), color - vec3(0.004));\n")
                out.append("return (color*(6.2*color+.5))/(color*(6.2*color+1.7)+0.06);\n")
            }
            ACESToneMap -> {
                out.append("const float A = 2.51;\n")
                out.append("const float B = 0.03;\n")
                out.append("const float C = 2.43;\n")
                out.append("const float D = 0.59;\n")
                out.append("const float E = 0.14;\n")
                out.append("color = clamp((color * (A * color + B)) / (color * (C * color + D) + E), 0.0, 1.0);\n")
                out.append("return pow(color, vec3($InvGamma));\n")
            }
            else -> "return pow(color, vec3($InvGamma));\n" // http://chilliant.blogspot.com/2012/08/srgb-approximations-for-hlsl.html
        })
        out.append("}\n")
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("${result.ref} = vec4(toneMap(${inputColor.asVec3()}), ${inputColor.ref}.a);\n")
    }

    fun set(other: ToneMapNode): ToneMapNode {
        toneMapType = other.toneMapType
        inputColor = other.inputColor
        return this
    }

    companion object {
        const val LinearToSRGBToneMap = 0
        const val UnchartedToneMap = 1
        const val HejlRichardToneMap = 2
        const val ACESToneMap = 3

        const val Gamma: Float = 2.2f
        const val InvGamma: Float = 1f / Gamma

        const val InputColor = "inputColor"

        private const val A = 0.15f
        private const val B = 0.50f
        private const val C = 0.10f
        private const val D = 0.20f
        private const val E = 0.02f
        private const val F = 0.30f
        private const val W = 11.2f
        private const val WhiteScale = 1f / (((W*(A*W+C*B)+D*E)/(W*(A*W+B)+D*F))-E/F)
    }
}