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
class GLSLValue(override var name: String, override var type: String): ShaderData() {
    override fun asFloat(): String = if (type == GLSLType.Float) ref else "$ref.x"
    override fun asVec2(): String = when(type) {
        GLSLType.Float -> "vec2($ref, 0.0)"
        GLSLType.Vec2 -> ref
        else -> "$ref.xy"
    }
    override fun asVec3(): String = when(type) {
        GLSLType.Float -> "vec3($ref, 0.0, 0.0)"
        GLSLType.Vec2 -> "vec3($ref, 0.0)"
        GLSLType.Vec3 -> ref
        else -> "$ref.xyz"
    }
    override fun asVec4(): String = when(type) {
        GLSLType.Float -> "vec4($ref, 0.0, 0.0, 1.0)"
        GLSLType.Vec2 -> "vec4($ref, 0.0, 1.0)"
        GLSLType.Vec3 -> "vec4($ref, 1.0)"
        else -> ref
    }
}
