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

import app.thelema.g3d.IUniformArgs
import app.thelema.img.ITexture
import app.thelema.img.ITexture2D
import app.thelema.img.TextureCube

/** @author zeganstyl */
abstract class TextureNode(textureType: String): ShaderNode() {
    var uv by input(GLSLNode.uv.uv)

    val sampler = GLSLValue("tex", textureType)
    val texColor = output(GLSLVec4("texColor"))
    val texAlpha = output(GLSLFloat("texAlpha").apply { scope = GLSLScope.Inline })

    protected abstract val tex: ITexture?

    /** If you use JPEG images, better to set it false */
    var sRGB: Boolean = true

    override fun prepareToBuild() {
        super.prepareToBuild()
        texAlpha.inlineCode = "${texColor.ref}.a"
    }

    override fun bind(uniforms: IUniformArgs) {
        super.bind(uniforms)

        val unit = shader.getNextTextureUnit()
        shader[sampler.ref] = unit
        tex?.bind(unit)
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("uniform ${sampler.typedRef};\n")
        out.append("${texColor.typedRef} = vec4(0.0);\n")
    }

    protected abstract fun getSamplerAccessCode(): String

    protected abstract fun getCoordinates(): String

    override fun executionFrag(out: StringBuilder) {
        out.append("${texColor.ref} = texture(${sampler.ref}, ${getCoordinates()});\n")
        if (sRGB) out.append("${texColor.ref}.xyz = pow(${texColor.ref}.xyz, vec3(2.2));\n")
    }
}

/** @author zeganstyl */
class Texture2DNode(): TextureNode(GLSLType.Sampler2D) {
    constructor(block: Texture2DNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "Texture2DNode"

    var texture: ITexture2D? = null
    override val tex: ITexture?
        get() = texture

    override fun getSamplerAccessCode(): String = "texture2D"
    override fun getCoordinates(): String = uv.asVec2()
}

/** @author zeganstyl */
class TextureCubeNode(): TextureNode(GLSLType.SamplerCube) {
    constructor(block: TextureCubeNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "TextureCubeNode"

    var texture: TextureCube? = null
    override val tex: TextureCube?
        get() = texture

    override fun getSamplerAccessCode(): String = "textureCube"
    override fun getCoordinates(): String = uv.asVec3()
}

/** @author zeganstyl */
class Texture3DNode(): TextureNode(GLSLType.Sampler3D) {
    constructor(block: Texture3DNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "Texture3DNode"

    var texture: ITexture? = null
    override val tex: ITexture?
        get() = texture

    override fun getSamplerAccessCode(): String = "texture3D"
    override fun getCoordinates(): String = uv.asVec3()
}