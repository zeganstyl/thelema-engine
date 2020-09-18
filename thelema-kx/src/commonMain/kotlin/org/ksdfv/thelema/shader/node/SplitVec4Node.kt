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
class SplitVec4Node(vector: IShaderData = GLSL.zeroFloat): ShaderNode() {
    override val name: String
        get() = "Split Vec4"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var vector
        get() = input[Vector] ?: GLSL.zeroFloat
        set(value) = setInput(Vector, value)

    val x = defOut(GLSLFloat("x").apply { scope = GLSLScope.Inline })
    val y = defOut(GLSLFloat("y").apply { scope = GLSLScope.Inline })
    val z = defOut(GLSLFloat("z").apply { scope = GLSLScope.Inline })
    val w = defOut(GLSLFloat("w").apply { scope = GLSLScope.Inline })

    init {
        setInput(Vector, vector)
    }

    override fun prepareToBuild() {
        super.prepareToBuild()
        setChannelNames()
    }

    private fun setChannelNames() {
        when (vector.type) {
            GLSLType.Float -> {
                x.inlineCode = vector.ref
                y.inlineCode = "0.0"
                z.inlineCode = "0.0"
                w.inlineCode = "1.0"
            }
            GLSLType.Vec2 -> {
                x.inlineCode = "${vector.ref}.x"
                y.inlineCode = "${vector.ref}.y"
                z.inlineCode = "0.0"
                w.inlineCode = "1.0"
            }
            GLSLType.Vec3 -> {
                x.inlineCode = "${vector.ref}.x"
                y.inlineCode = "${vector.ref}.y"
                z.inlineCode = "${vector.ref}.z"
                w.inlineCode = "1.0"
            }
            else -> {
                x.inlineCode = "${vector.ref}.x"
                y.inlineCode = "${vector.ref}.y"
                z.inlineCode = "${vector.ref}.z"
                w.inlineCode = "${vector.ref}.w"
            }
        }
    }

    companion object {
        const val ClassId = "splitVec4"

        const val Vector = "vector"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(Vector, GLSLType.Vec4)
        }
    }
}