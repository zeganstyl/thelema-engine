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

package app.thelema.math

/** @author zeganstyl */
open class Rectangle(
    override var x: Float = 0f,
    override var y: Float = 0f,
    override var width: Float = 0f,
    override var height: Float = 0f
): IRectangle {
    /** Converts this `Rectangle` to a string in the format `[x,y,width,height]`. */
    override fun toString() = "[$x,$y,$width,$height]"
}
