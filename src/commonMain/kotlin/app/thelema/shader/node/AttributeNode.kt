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

/**
 * @param type use [GLSLType]
 * @author zeganstyl */
class AttributeNode(
    attributeName: String = "",
    type: Int = GLSLType.Float
): ShaderNode() {
    override val name: String
        get() = "Attribute"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var type: Int
        get() = this.value.type
        set(value) {
            this.value.type = value
        }

    var attributeName: String = attributeName
        set(value) {
            field = value
            this.value.name = "v$value"
        }

    val value = GLSLValue("aValue", type).apply { defOut(this) }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        attributeName = json.string("attributeName", "")
        type = GLSLType.getTypeByName(json.string("type"))
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["attributeName"] = attributeName
        json["type"] = GLSLType.getTypeName(type)
    }

    override fun declarationVert(out: StringBuilder) {
        super.declarationVert(out)
        out.append("attribute ${value.typeStr} $attributeName;\n")
        out.append("varying ${value.typedRef};\n")
    }

    override fun declarationFrag(out: StringBuilder) {
        super.declarationFrag(out)
        out.append("varying ${value.typedRef};\n")
    }

    override fun executionVert(out: StringBuilder) {
        super.executionVert(out)
        out.append("${value.ref} = $attributeName;\n")
    }

    companion object {
        const val ClassId = "attribute"

        val InputForm = HashMap<String, Int>()
    }
}