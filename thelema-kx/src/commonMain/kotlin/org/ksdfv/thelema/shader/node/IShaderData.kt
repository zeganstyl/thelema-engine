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

import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
interface IShaderData {
    var name: String

    /** Use this for referencing to this variable. Reference will be an unique name in shader program. */
    val ref: String
        get() = if (scope == GLSLScope.Inline) inlineCode else "$name${container?.shader?.getUID(this) ?: ""}"

    /** Float number representation of ref */
    val fRef: String
        get() {
            val ref = ref
            return if (ref.contains('.')) ref else "$ref.0"
        }

    var inlineCode: String

    /** Use [GLSLScope] */
    var scope: Int

    /** Use [GLSLType] */
    val type: Int

    /** Reserved. Do not set it manually */
    var container: IShaderNode?

    /** Connections to other nodes inputs. Do not change it manually. */
    val connectedTo: MutableList<ShaderNodeLink>

    val isUsed: Boolean
        get() = connectedTo.isNotEmpty()

    val typeStr: String
        get() = when (type) {
            GLSLType.Bool -> "bool"
            GLSLType.Int -> "int"
            GLSLType.Float -> "float"
            GLSLType.Vec2 -> "vec2"
            GLSLType.Vec3 -> "vec3"
            GLSLType.Vec4 -> "vec4"
            GLSLType.Mat2 -> "mat2"
            GLSLType.Mat3 -> "mat3"
            GLSLType.Mat4 -> "mat4"
            GLSLType.Sampler1D -> "sampler1D"
            GLSLType.Sampler2D -> "sampler2D"
            GLSLType.Sampler3D -> "sampler3D"
            GLSLType.Sampler2DArray -> "sampler2DArray"
            GLSLType.SamplerCube -> "samplerCube"
            else -> ""
        }

    /** May be used for declaration */
    val typedRef: String
        get() = "$typeStr $ref"

    fun declaration(): String {
        return if (scope == GLSLScope.Inline) {
            LOG.info("Inline value can't be declared, shader node: ${container?.name}")
            ""
        } else {
            val scope = GLSLScope.getTypeText(scope, container?.shader?.version ?: 110)
            if (scope.isNotEmpty()) "$scope $typedRef" else typedRef
        }
    }

    fun asFloat(): String = throw NotImplementedError()
    fun asVec2(): String = throw NotImplementedError()
    fun asVec3(): String = throw NotImplementedError()
    fun asVec4(): String = throw NotImplementedError()

    fun asTyped(type: Int): String {
        return when (type) {
            GLSLType.Float -> asFloat()
            GLSLType.Vec2 -> asVec2()
            GLSLType.Vec3 -> asVec3()
            GLSLType.Vec4 -> asVec4()
            else -> throw IllegalArgumentException()
        }
    }

    fun set(other: IShaderData): IShaderData {
        name = other.name
        inlineCode = other.inlineCode
        scope = other.scope
        return this
    }
}