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

package app.thelema.lwjgl3

/** Class describing the bits per pixel, depth buffer precision, stencil precision and number of MSAA samples.  */
class BufferFormat(/* number of bits per color channel */
        val r: Int, val g: Int, val b: Int, val a: Int, /* number of bits for depth and stencil buffer */
        val depth: Int, val stencil: Int,
        /** number of samples for multi-sample anti-aliasing (MSAA)  */
        val samples: Int,
        /** whether coverage sampling anti-aliasing is used. in that case you have to clear the coverage buffer as well!  */
        val coverageSampling: Boolean) {

    override fun toString(): String {
        return ("r: " + r + ", g: " + g + ", b: " + b + ", a: " + a + ", depth: " + depth + ", stencil: " + stencil
                + ", num samples: " + samples + ", coverage sampling: " + coverageSampling)
    }

}