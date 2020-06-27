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

package org.ksdfv.thelema.math

/** Objects, that may be needed in calculations.
 * May give wrong result while multithreading. */
object Temp {
    val vec2 = Vec2()
    val vec3 = Vec3()
    val vec3n2 = Vec3()
    val vec4 = Vec4()
    val vec4n2 = Vec4()
    val mat3 = Mat3()
    val mat4 = Mat4()
}