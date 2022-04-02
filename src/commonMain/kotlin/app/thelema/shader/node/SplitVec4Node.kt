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
class SplitVec4Node(vector: IShaderData = GLSL.zeroFloat): ShaderNode() {
    override val componentName: String
        get() = "SplitVec4Node"

    var vector by input(GLSL.zeroFloat)

    val x = output(GLSLFloat("x").apply { scope = GLSLScope.Inline })
    val y = output(GLSLFloat("y").apply { scope = GLSLScope.Inline })
    val z = output(GLSLFloat("z").apply { scope = GLSLScope.Inline })
    val w = output(GLSLFloat("w").apply { scope = GLSLScope.Inline })

    init {
        this.vector = vector
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
}