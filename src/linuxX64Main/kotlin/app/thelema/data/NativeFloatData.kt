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

package app.thelema.data
import kotlinx.cinterop.*
import app.thelema.data.IFloatData

class NativeFloatData(
    capacity: Int,
    ptr: CPointer<FloatVar> = nativeHeap.allocArray(capacity)
): NativeDataArray<Float, FloatVar>(capacity, ptr), IFloatData {
    override fun set(index: Int, value: Float) {
        ptr[index] = value
    }

    override fun get(index: Int): Float = ptr[index]
}