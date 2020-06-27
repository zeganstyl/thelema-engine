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

package org.ksdfv.thelema

import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec4

interface ImmediateModeRenderer {
    fun begin(projModelView: IMat4, primitiveType: Int)
    fun flush()
    fun color(color: IVec4)
    fun color(r: Float, g: Float, b: Float, a: Float)
    fun color(colorBits: Float)
    fun texCoord(u: Float, v: Float)
    fun normal(x: Float, y: Float, z: Float)
    fun vertex(x: Float, y: Float, z: Float)
    fun end()
    val numVertices: Int
    val maxVertices: Int

    fun dispose()
}
