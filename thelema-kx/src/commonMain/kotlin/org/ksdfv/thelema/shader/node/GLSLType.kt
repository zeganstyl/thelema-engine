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

/** [GLSL documentation](https://www.khronos.org/opengl/wiki/Data_Type_(GLSL))
 * @author zeganstyl */
object GLSLType {
    const val Bool = 0
    const val Int = 1
    const val Float = 3
    const val Vec2 = 4
    const val Vec3 = 5
    const val Vec4 = 6
    const val Mat2 = 7
    const val Mat3 = 8
    const val Mat4 = 9
    const val Mat3x4 = 10
    const val Sampler1D = 11
    const val Sampler2D = 12
    const val Sampler3D = 13
    const val SamplerCube = 14
    const val Sampler2DArray = 15

    fun getTypeName(type: Int): String = when (type) {
        Bool -> "bool"
        Int -> "int"
        Float -> "float"
        Vec2 -> "vec2"
        Vec3 -> "vec3"
        Vec4 -> "vec4"
        Mat2 -> "mat2"
        Mat3 -> "mat3"
        Mat4 -> "mat4"
        Mat3x4 -> "mat3x4"
        Sampler1D -> "sampler1D"
        Sampler2D -> "sampler2D"
        Sampler3D -> "sampler3D"
        SamplerCube -> "samplerCube"
        Sampler2DArray -> "sampler2DArray"
        else -> ""
    }

    fun getTypeByName(name: String): Int = when (name) {
        "bool" -> Bool
        "int" -> Int
        "float" -> Float
        "vec2" -> Vec2
        "vec3" -> Vec3
        "vec4" -> Vec4
        "mat2" -> Mat2
        "mat3" -> Mat3
        "mat4" -> Mat4
        "mat3x4" -> Mat3x4
        "sampler1D" -> Sampler1D
        "sampler2D" -> Sampler2D
        "sampler3D" -> Sampler3D
        "samplerCube" -> SamplerCube
        "sampler2DArray" -> Sampler2DArray
        else -> -1
    }
}