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

package org.ksdfv.thelema.shader.node

/** @author zeganstyl */
class MergeVec4(
    x: IShaderData = GLSL.zeroFloat,
    y: IShaderData = GLSL.zeroFloat,
    z: IShaderData = GLSL.zeroFloat,
    w: IShaderData = GLSL.oneFloat
): ShaderNode() {
    override val name: String
        get() = "Merge Vec4"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var x: IShaderData
        get() = input[X] ?: GLSL.zeroFloat
        set(value) = setInput(X, value)

    var y: IShaderData
        get() = input[Y] ?: GLSL.zeroFloat
        set(value) = setInput(Y, value)

    var z: IShaderData
        get() = input[Z] ?: GLSL.zeroFloat
        set(value) = setInput(Z, value)

    var w: IShaderData
        get() = input[W] ?: GLSL.oneFloat
        set(value) = setInput(W, value)

    val result = defOut(GLSLValue("result", GLSLType.Vec4).apply {
        inlineCode = "vec4(${x.asFloat()}, ${y.asFloat()}, ${z.asFloat()}, ${w.asFloat()})"
        scope = GLSLScope.Inline
    })

    init {
        setInput(X, x)
        setInput(Y, y)
        setInput(Z, z)
        setInput(W, w)
    }

    override fun prepareToBuild() {
        super.prepareToBuild()
        result.inlineCode = "vec4(${x.asFloat()}, ${y.asFloat()}, ${z.asFloat()}, ${w.asFloat()})"
    }

    companion object {
        const val ClassId = "mergeVec4"

        const val X = "x"
        const val Y = "y"
        const val Z = "z"
        const val W = "w"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(X, GLSLType.Float)
            put(Y, GLSLType.Float)
            put(Z, GLSLType.Float)
            put(W, GLSLType.Float)
        }
    }
}
