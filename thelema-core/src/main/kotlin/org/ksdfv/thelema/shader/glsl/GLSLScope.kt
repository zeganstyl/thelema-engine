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
object GLSLScope {
    /** Value will not be stored in variables and will be passed as is */
    const val Inline = 0

    /** Value will be stored as local variable in main function */
    const val Local = 1

    /** Value will be stored as global for one shader program in declaration section */
    const val Global = 2

    const val Uniform = 3

    const val Attribute = 4

    const val VaryingIn = 5
    const val VaryingOut = 6

    fun getTypeText(type: Int, glslVersion: Int = 110): String = when (type) {
        Attribute -> if (glslVersion >= 130) "in" else "attribute"
        VaryingIn -> if (glslVersion >= 130) "in" else "varying"
        VaryingOut -> if (glslVersion >= 130) "out" else "varying"
        Uniform -> "uniform"
        else -> ""
    }
}