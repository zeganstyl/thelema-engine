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

import app.thelema.json.IJsonObject

/** @author zeganstyl */
class UVNode() : ShaderNode() {
    constructor(block: UVNode.() -> Unit): this() { block(this) }

    override val name: String
        get() = "UV"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var aUVName: String = "TEXCOORD_0"

    val uv = defOut(GLSLVec2("uv"))

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        aUVName = json.string("aUVName", "TEXCOORD_0")
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["aUVName"] = aUVName
    }

    override fun declarationFrag(out: StringBuilder) {
        if (uv.isUsed) out.append("$varIn ${uv.typedRef};\n")
    }

    override fun executionVert(out: StringBuilder) {
        if (uv.isUsed) out.append("${uv.ref} = $aUVName;\n")
    }

    override fun declarationVert(out: StringBuilder) {
        if (uv.isUsed) {
            out.append("$attribute ${uv.typeStr} $aUVName;\n")
            out.append("$varOut ${uv.typedRef};\n")
        }
    }

    companion object {
        const val ClassId = "uv"

        val InputForm = LinkedHashMap<String, Int>()
    }
}