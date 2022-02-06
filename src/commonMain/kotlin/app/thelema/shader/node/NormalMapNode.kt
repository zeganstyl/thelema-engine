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
class NormalMapNode(): ShaderNode() {
    constructor(block: NormalMapNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "NormalMapNode"

    /** Optional. Normal scaling float value */
    var scale by input(GLSL.oneFloat)

    /** Normal from texture */
    var color by input(GLSL.defaultNormal)

    /** Matrix 3x3 with tangent, binormal, normal vectors */
    var tbn by input(GLSLNode.vertex.tbn)

    val normal = output(GLSLVec3("normal"))

    override fun declarationFrag(out: StringBuilder) {
        if (normal.isUsed) {
            out.append(normal.typedRefEnd)
            out.append("""
vec3 normalMapMain(mat3 tbn, float normalScale, vec3 colorValue) {
    vec3 t = tbn[0];
    vec3 b = tbn[1];
    vec3 ng = tbn[2];

    // For a back-facing surface, the tangential basis vectors are negated.
    if (gl_FrontFacing == false) {
        t *= -1.0;
        b *= -1.0;
        ng *= -1.0;
    }

    vec3 n = colorValue * 2.0 - vec3(1.0);
    n *= vec3(normalScale, normalScale, 1.0);
    n = mat3(t, b, ng) * normalize(n);

    return n;
}
""")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        if (normal.isUsed) {
            out.append("${normal.ref} = normalMapMain(${tbn.ref}, ${scale.asFloat()}, ${color.asVec3()});\n")
        }
    }
}
