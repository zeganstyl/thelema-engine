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
import app.thelema.img.ITexture
import app.thelema.json.IJsonObject
import app.thelema.gl.IMesh

/** @author zeganstyl */
class TextureNode(): ShaderNode() {
    constructor(block: TextureNode.() -> Unit): this() { block(this) }

    constructor(
        uv: IShaderData = GLSL.zeroFloat,
        texture: ITexture? = null,

        /** If you use JPEG images, better to set it false */
        sRGB: Boolean = true,

        /** Use [GLSLType] */
        textureType: String = GLSLType.Sampler2D
    ): this() {
        this.uv = uv
        this.texture = texture
        this.sRGB = sRGB
        this.textureType = textureType
    }

    override val name: String
        get() = "Texture"

    var uv
        get() = input["uv"] ?: GLSL.zeroFloat
        set(value) = setInput("uv", value)

    val sampler = defOut(GLSLFloat("tex"))
    val color = defOut(GLSLVec4("texColor"))
    val alpha = defOut(GLSLFloat("texAlpha").apply { scope = GLSLScope.Inline })

    var texture: ITexture? = null

    /** If you use JPEG images, better to set it false */
    var sRGB: Boolean = true

    /** Use [GLSLType] */
    var textureType: String = GLSLType.Sampler2D

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        sRGB = json.bool("sRGB", true)
        textureType = json.string("textureType", GLSLType.Sampler2D)
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["sRGB"] = sRGB
        json["textureType"] = textureType
    }

    override fun prepareToBuild() {
        super.prepareToBuild()
        alpha.inlineCode = "${color.ref}.a"
    }

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        super.prepareShaderNode(mesh, scene)

        if (color.isUsed || alpha.isUsed) {
            val unit = shader.getNextTextureUnit()
            shader[sampler.ref] = unit
            texture?.bind(unit)
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("uniform $textureType ${sampler.ref};\n")
        out.append("${color.typedRef} = vec4(0.0);\n")
    }

    private fun getSamplerAccessCode(type: String): String {
        return if (shader.version >= 130) "texture" else when (type) {
            GLSLType.Sampler2D -> "texture2D"
            GLSLType.Sampler3D -> "texture3D"
            GLSLType.SamplerCube -> "textureCube"
            else -> "texture"
        }
    }

    private fun getCoordinates(type: String): String = when (type) {
        GLSLType.Sampler2D -> uv.asVec2()
        GLSLType.SamplerCube -> uv.asVec3()
        else -> uv.ref
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("${color.ref} = ${getSamplerAccessCode(textureType)}(${sampler.ref}, ${getCoordinates(textureType)});\n")
        if (sRGB) out.append("${color.ref}.xyz = pow(${color.ref}.xyz, vec3(2.2));\n")
    }

    fun set(other: TextureNode): TextureNode {
        texture = other.texture
        textureType = other.textureType
        sRGB = other.sRGB
        return this
    }
}
