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

package org.ksdfv.thelema.kxnative.data
import kotlinx.cinterop.*
import org.ksdfv.thelema.data.IShortData

class NativeShortData(
    capacity: Int,
    ptr: CPointer<ShortVar> = nativeHeap.allocArray(capacity)
): NativeDataArray<Short, ShortVar>(capacity, ptr), IShortData {
    override fun set(index: Int, value: Short) {
        ptr[index] = value
    }

    override fun get(index: Int): Short = ptr[index]
}