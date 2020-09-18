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

package org.ksdfv.thelema.jvm.data

import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.jvm.data.JvmBuffer
import java.nio.Buffer
import java.nio.FloatBuffer

/** @author zeganstyl */
class JvmFloatBuffer(val floatBuffer: FloatBuffer): IFloatData, JvmBuffer<Float>() {
    override val buffer: Buffer
        get() = floatBuffer

    override fun set(index: Int, value: Float) {
        floatBuffer.put(index, value)
    }

    override fun get(index: Int): Float = floatBuffer.get(index)

    override fun get(): Float = floatBuffer.get()

    override fun put(value: Float) {
        floatBuffer.put(value)
    }
}