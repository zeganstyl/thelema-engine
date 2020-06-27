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

package org.ksdfv.thelema.jvm

import org.ksdfv.thelema.data.IShortData
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/** @author zeganstyl */
class JvmShortBuffer(val shortBuffer: ShortBuffer): JvmBuffer<Short>(), IShortData {
    constructor(capacity: Int): this(ByteBuffer.allocateDirect(capacity * 2).order(ByteOrder.nativeOrder()).asShortBuffer())

    override val buffer: Buffer
        get() = shortBuffer

    override fun set(index: Int, value: Short) {
        shortBuffer.put(index, value)
    }

    override fun get(): Short = shortBuffer.get()

    override fun get(index: Int): Short = shortBuffer.get(index)

    override fun put(value: Short) {
        shortBuffer.put(value)
    }
}