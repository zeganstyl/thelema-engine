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

package org.ksdfv.thelema.shader.glsl

/** @author zeganstyl */
class GLSLVec3Inline(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    override var name: String = ""
): ShaderData() {
    override var inlineCode: String
        get() = asVec3()
        set(_) {}

    override val type: Int
        get() = GLSLType.Vec3

    override var scope: Int
        get() = GLSLScope.Inline
        set(_) {}

    override fun asFloat(): String = x.toString()
    override fun asVec2(): String = "vec2($x, $y)"
    override fun asVec3(): String = "vec3($x, $y, $z)"
    override fun asVec4(): String = "vec4($x, $y, $z, 1.0)"
}