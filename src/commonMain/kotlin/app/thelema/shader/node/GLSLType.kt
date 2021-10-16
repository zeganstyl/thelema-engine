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

/** [GLSL documentation](https://www.khronos.org/opengl/wiki/Data_Type_(GLSL))
 *
 * @author zeganstyl */
object GLSLType {
    const val Bool = "bool"
    const val Int = "int"
    const val Float = "float"
    const val Vec2 = "vec2"
    const val Vec3 = "vec3"
    const val Vec4 = "vec4"
    const val Mat2 = "mat2"
    const val Mat3 = "mat3"
    const val Mat4 = "mat4"
    const val Mat3x4 = "mat3x4"
    const val Sampler1D = "sampler1D"
    const val Sampler2D = "sampler2D"
    const val Sampler3D = "sampler3D"
    const val SamplerCube = "samplerCube"
    const val Sampler2DArray = "sampler2DArray"
}