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

/** @author zeganstyl */
class UVNode() : ShaderNode() {
    constructor(block: UVNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "UVNode"

    var uvName: String = "TEXCOORD_0"

    val uv = output(GLSLVec2("uv"))

    override fun declarationFrag(out: StringBuilder) {
        if (uv.isUsed) out.append("in ${uv.typedRef};\n")
    }

    override fun executionVert(out: StringBuilder) {
        if (uv.isUsed) out.append("${uv.ref} = $uvName;\n")
    }

    override fun declarationVert(out: StringBuilder) {
        if (uv.isUsed) {
            out.append("in ${uv.typeStr} $uvName;\n")
            out.append("out ${uv.typedRef};\n")
        }
    }
}