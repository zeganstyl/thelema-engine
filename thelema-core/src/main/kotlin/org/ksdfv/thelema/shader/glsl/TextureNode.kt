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

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.shader.IShader
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
class TextureNode(
    uv: IShaderData = GLSL.zeroFloat,
    var texture: ITexture? = null,

    /** If you use JPEG images, better to set it false */
    var sRGB: Boolean = true,

    /** Use [GLSLType] */
    var textureType: Int = GLSLType.Sampler2D
): ShaderNode() {
    override val name: String
        get() = "Texture"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    val unit = HashMap<IShader, Int>()

    var uv
        get() = input[UV] ?: GLSL.zeroFloat
        set(value) = setInput(UV, value)

    val sampler = defOut(GLSLFloat("tex"))
    val color = defOut(GLSLVec4("texColor"))
    val alpha = defOut(GLSLFloat("texAlpha").apply { scope = GLSLScope.Inline })

    init {
        setInput(UV, uv)
    }

    override fun read(json: IJsonObject) {
        super.read(json)

        sRGB = json.bool("sRGB", true)
        textureType = GLSLType.getTypeByName(json.string("textureType", GLSLType.getTypeName(GLSLType.Sampler2D)))
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        json["sRGB"] = sRGB
        json["textureType"] = GLSLType.getTypeName(textureType)
    }

    override fun prepareToBuild() {
        super.prepareToBuild()
        unit[shader] = if (color.isUsed) GL.grabTextureUnit() else 0
        alpha.inlineCode = "${color.ref}.a"
    }

    override fun shaderCompiled() {
        val u = unit[shader] ?: 0
        shader[sampler.ref] = u
    }

    override fun prepareToDrawMesh(mesh: IMesh) {
        super.prepareToDrawMesh(mesh)
        texture?.bind(unit[shader] ?: 0)
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("uniform ${GLSLType.getTypeName(textureType)} ${sampler.ref};\n")
        out.append("${color.typedRef} = vec4(0.0);\n")
    }

    private fun getSamplerAccessCode(type: Int): String {
        return if (shader.version >= 130) "texture" else when (type) {
            GLSLType.Sampler2D -> "texture2D"
            GLSLType.Sampler3D -> "texture3D"
            GLSLType.SamplerCube -> "textureCube"
            else -> "texture"
        }
    }

    private fun getCoordinates(type: Int): String = when (type) {
        GLSLType.Sampler2D -> uv.asVec2()
        GLSLType.SamplerCube -> uv.asVec3()
        else -> uv.ref
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("${color.ref} = ${getSamplerAccessCode(textureType)}(${sampler.ref}, ${getCoordinates(textureType)});\n")
        if (sRGB) out.append("${color.ref}.xyz = pow(${color.ref}.xyz, vec3(2.2));\n")
    }

    fun set(other: TextureNode): TextureNode {
        unit.clear()
        unit.putAll(other.unit)

        texture = other.texture
        textureType = other.textureType
        sRGB = other.sRGB
        return this
    }

    companion object {
        const val ClassId = "texture"

        const val UV = "uv"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(UV, GLSLType.Vec2)
        }
    }
}
