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

import org.ksdfv.thelema.data.IDataArray
import java.nio.Buffer

/** @author zeganstyl */
abstract class JvmBuffer<T>: IDataArray<T> {
    abstract val buffer: Buffer

    override val sourceObject: Any
        get() = buffer

    override var size: Int
        get() = buffer.limit()
        set(value) { buffer.limit(value) }

    override var position: Int
        get() = buffer.position()
        set(value) { buffer.position(value) }

    override val capacity: Int
        get() = buffer.capacity()

    override fun rewind() {
        buffer.rewind()
    }

    override fun flip() {
        buffer.flip()
    }
}