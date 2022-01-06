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

import app.thelema.math.IVec3
import app.thelema.math.Vec3

/** @author zeganstyl */
class GLSLVec3Literal(): GLSLLiteralBase() {
    constructor(x: Float, y: Float, z: Float): this() {
        value.set(x, y, z)
    }

    override var inlineCode: String
        get() = asVec3()
        set(_) {}

    override val type: String
        get() = GLSLType.Vec3

    override val componentName: String
        get() = "GLSLVec3Literal"

    var value: IVec3 = Vec3()
        set(value) { field.set(value) }

    override fun asFloat(): String = str(value.x)
    override fun asVec2(): String = "vec2(${str(value.x)}, ${str(value.y)})"
    override fun asVec3(): String = "vec3(${str(value.x)}, ${str(value.y)}, ${str(value.z)})"
    override fun asVec4(): String = "vec4(${str(value.x)}, ${str(value.y)}, ${str(value.z)}, 1.0)"
}