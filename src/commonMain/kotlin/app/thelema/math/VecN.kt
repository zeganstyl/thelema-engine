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
class VecN(numComponents: Int): IVec {
    val values = ArrayList<Float>()

    override var numComponents: Int = numComponents
        set(value) {
            if (field != value && value > 0) {
                field = value
                if (values.size > value) {
                    val diff = values.size - value
                    for (i in 0 until diff) {
                        values.removeAt(values.lastIndex)
                    }
                } else if (values.size < value) {
                    val diff = value - values.size
                    for (i in 0 until diff) {
                        values.add(0f)
                    }
                }
            }
        }

    override fun getComponent(index: Int): Float = values[index]

    override fun setComponent(index: Int, value: Float) {
        values[index] = value
    }

    override fun copy(): IVec = VecN(numComponents).set(this)

    override fun toString(): String {
        var str = "("
        str += values[0]
        for (i in 1 until values.size) {
            str += ","
            str += values[i]
        }
        str += ")"
        return str
    }
}