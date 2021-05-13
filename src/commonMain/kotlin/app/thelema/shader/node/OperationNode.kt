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

/** With this node there is ability to define any operation on input data.
 *
 * If you want change single argument, you can use `setInput("arg1", value)`
 *
 * @author zeganstyl */
class OperationNode(
    args: List<IShaderData> = ArrayList(),

    /** You can write complex calculation here, See [GLSL operations](https://en.wikibooks.org/wiki/GLSL_Programming/Vector_and_Matrix_Operations).
     *
     * `args[0]`, `args[1]` will be as arg1, arg2 and etc.
     *
     * Arguments will not be converted to result type, you must do it by yourself in code. */
    var function: String = "vec4(arg1.xyz + arg2.xyz, arg1.a)",

    /** See [GLSLType] */
    resultType: Int = args.getOrNull(0)?.type ?: GLSLType.Float
): ShaderNode() {
    override val name: String
        get() = "Operation"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var resultType: Int
        get() = result.type
        set(value) { result.type = value }

    val result = GLSLValue("op", resultType).apply { defOut(this) }

    var isFragment = true
    var isVarying = true

    init {
        setArgs(args)
    }

    /** `args[0]`, `args[1]` will be as arg1, arg2 and etc. */
    fun setArgs(args: List<IShaderData>) {
        inputInternal.clear()
        for (i in args.indices) {
            setInput("arg${i+1}", args[i])
        }
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        function = json.string("function", "")
        isFragment = json.bool("isFragment", true)
        isVarying = json.bool("isVarying", true)
        resultType = GLSLType.getTypeByName(json.string("resultType"))
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json["function"] = function
        json["isFragment"] = isFragment
        json["isVarying"] = isVarying
        json["resultType"] = GLSLType.getTypeName(resultType)
    }

    override fun executionVert(out: StringBuilder) {
        if (!isFragment) {
            var f = function
            input.forEach {
                f = f.replace(it.key, it.value.ref)
            }
            if (isVarying) {
                out.append("${result.ref} = $f;\n")
            } else {
                out.append("${result.typedRef} = $f;\n")
            }
        }
    }

    override fun declarationVert(out: StringBuilder) {
        super.declarationVert(out)

        if (isVarying) {
            out.append("varying ${result.typedRef};\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        super.declarationFrag(out)

        if (isVarying) {
            out.append("varying ${result.typedRef};\n")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        if (isFragment) {
            var f = function
            input.forEach {
                f = f.replace(it.key, it.value.ref)
            }
            out.append("${result.typedRef} = $f;\n")
        }
    }

    companion object {
        const val ClassId = "operation"

        val InputForm = HashMap<String, Int>()
    }
}