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
class GLSLVec3(override var name: String): ShaderData() {
    override val type: Int
        get() = GLSLType.Vec3

    override fun asFloat(): String = "$ref.x"
    override fun asVec2(): String = "$ref.xy"
    override fun asVec3(): String = ref
    override fun asVec4(): String = "vec4($ref, 1.0)"
}