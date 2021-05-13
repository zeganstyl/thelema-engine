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

package app.thelema.ui

/** Takes a linear value in the range of 0-1 and outputs a (usually) non-linear, interpolated value.
 * @author Nathan Sweet
 */
abstract class Interpolation {
    /** @param a Alpha value between 0 and 1.
     */
    abstract fun apply(a: Float): Float

    /** @param a Alpha value between 0 and 1.
     */
    fun apply(start: Float, end: Float, a: Float): Float {
        return start + (end - start) * apply(a)
    }

    companion object {
        //
        val linear: Interpolation = object : Interpolation() {
            override fun apply(a: Float): Float {
                return a
            }
        }
        /** By Ken Perlin.  */
        val smoother: Interpolation = object : Interpolation() {
            override fun apply(a: Float): Float {
                return a * a * a * (a * (a * 6 - 15) + 10)
            }
        }
        val fade = smoother
    }
}
