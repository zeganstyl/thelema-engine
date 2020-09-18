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
class GLSLVec4Inline(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 0f,
    override var name: String = ""
): GLSLInlineBase() {
    override var inlineCode: String
        get() = asVec4()
        set(_) {}

    override val type: Int
        get() = GLSLType.Vec4

    override var scope: Int
        get() = GLSLScope.Inline
        set(_) {}

    override fun asFloat(): String = str(x)
    override fun asVec2(): String = "vec2(${str(x)}, ${str(y)})"
    override fun asVec3(): String = "vec3(${str(x)}, ${str(y)}, ${str(z)})"
    override fun asVec4(): String = "vec4(${str(x)}, ${str(y)}, ${str(z)}, ${str(w)})"
}