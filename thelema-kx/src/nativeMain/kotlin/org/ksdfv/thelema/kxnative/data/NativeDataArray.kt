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
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import org.ksdfv.thelema.data.IDataArray

abstract class NativeDataArray<T, C: CPointed>(
    capacity: Int,
    val ptr: CPointer<C>
): IDataArray<T> {
    override val capacity: Int = capacity

    override var size: Int = capacity

    override var position: Int = 0
        set(value) {
            field = value
            posPtr = (ptr.toLong() + value).toCPointer()!!
        }

    var posPtr: CPointer<C> = ptr

    override val sourceObject: Any
        get() = posPtr
}