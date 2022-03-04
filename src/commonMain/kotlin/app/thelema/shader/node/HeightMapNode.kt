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
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.img.ITexture2D

/** @author zeganstyl */
class HeightMapNode: ShaderNode() {
    override val componentName: String
        get() = "HeightMapNode"

    /** Component from [inputPosition] will be used as U texture coordinate */
    var u = "x"
    /** Component from [inputPosition] will be used as V texture coordinate */
    var v = "z"

    var texture: ITexture2D? = null

    var uvScale: Float = 1f

    var inputPosition by input(GLSLNode.vertex.position)

    var mapWorldSize: IVec3 = Vec3(1f, 1f, 1f)
        set(value) { field.set(value) }

    private val sampler = GLSLValue("tex", GLSLType.Sampler2D)
    val texColor = output(GLSLVec4("texColor"))
    val height = output(GLSLFloat("height"))
    val outputPosition = output(GLSLVec3("outputPosition"))
    val uv = output(GLSLVec2("uv"))

    override fun bind(uniforms: IUniformArgs) {
        super.bind(uniforms)

        val unit = shader.getNextTextureUnit()
        shader[sampler.ref] = unit
        texture?.bind(unit)
    }

    override fun declarationVert(out: StringBuilder) {
        super.declarationVert(out)
        out.append(sampler.uniformRefEnd)
        out.append(texColor.outRefEnd)
        out.append(height.outRefEnd)
        out.append(outputPosition.outRefEnd)
        out.append(uv.outRefEnd)
    }

    override fun declarationFrag(out: StringBuilder) {
        super.declarationFrag(out)
        out.append(texColor.inRefEnd)
        out.append(height.inRefEnd)
        out.append(outputPosition.inRefEnd)
        out.append(uv.inRefEnd)
    }

    override fun executionVert(out: StringBuilder) {
        super.executionVert(out)
        val pos = inputPosition.ref
        out.append("${uv.ref} = vec2($pos.$u / ${mapWorldSize.x} + 0.5, $pos.$v / ${mapWorldSize.z} + 0.5) * $uvScale;\n")
        out.append("${texColor.ref} = texture2D(${sampler.ref}, ${uv.ref});\n")
        out.append("${height.ref} = ${texColor.ref}.x * ${mapWorldSize.y};\n")
        out.append("${outputPosition.ref} = $pos;\n")
        out.append("${outputPosition.ref}.y = ${height.ref};\n")
    }
}