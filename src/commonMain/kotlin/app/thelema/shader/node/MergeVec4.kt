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
class MergeVec4(): ShaderNode() {
    constructor(
        x: IShaderData = GLSL.zeroFloat,
        y: IShaderData = GLSL.zeroFloat,
        z: IShaderData = GLSL.zeroFloat,
        w: IShaderData = GLSL.oneFloat
    ): this() {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override val componentName: String
        get() = "MergeVec4"

    var x by input(GLSL.zeroFloat)
    var y by input(GLSL.zeroFloat)
    var z by input(GLSL.zeroFloat)
    var w by input(GLSL.oneFloat)

    val result = output(GLSLValue("result", GLSLType.Vec4).apply {
        inlineCode = "vec4(${x.asFloat()}, ${y.asFloat()}, ${z.asFloat()}, ${w.asFloat()})"
        scope = GLSLScope.Inline
    })

    override fun prepareToBuild() {
        super.prepareToBuild()
        result.inlineCode = "vec4(${x.asFloat()}, ${y.asFloat()}, ${z.asFloat()}, ${w.asFloat()})"
    }
}
