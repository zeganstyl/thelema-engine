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

package app.thelema.jvm.data

import app.thelema.data.IIntData
import app.thelema.jvm.data.JvmBuffer
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

/** @author zeganstyl */
class JvmIntBuffer(val intBuffer: IntBuffer): IIntData, JvmBuffer() {
    constructor(capacity: Int): this(ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder()).asIntBuffer())

    override val buffer: Buffer
        get() = intBuffer

    override fun set(index: Int, value: Int) {
        intBuffer.put(index, value)
    }

    override fun get(): Int = intBuffer.get()

    override fun get(index: Int): Int = intBuffer.get(index)

    override fun put(value: Int) {
        intBuffer.put(value)
    }
}