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

package app.thelema

import kotlinx.cinterop.*
import app.thelema.data.IByteData
import app.thelema.data.IFloatData
import app.thelema.data.IIntData
import app.thelema.data.IShortData
import app.thelema.data.NativeByteData
import app.thelema.data.NativeFloatData
import app.thelema.data.NativeIntData
import app.thelema.data.NativeShortData

val uint1 = nativeHeap.alloc<UIntVar>()
val uint1Ptr = uint1.ptr

val int1 = nativeHeap.alloc<IntVar>()
val int1Ptr = int1.ptr

val ptrVar = nativeHeap.alloc<COpaquePointerVar>()
val ptrVarPtr = ptrVar.ptr

inline fun Int.uint() = toUInt()
inline fun Int.uintPtr(): CPointer<UIntVar> {
    uint1.value = toUInt()
    return uint1Ptr
}

inline fun Boolean.ubyte() = toByte().toUByte()
inline fun IntArray.uintPtr() = toUIntArray().toCValues()

inline fun UInt.sint() = toInt()
inline fun UByte.bool() = toInt() == 1

inline fun uintOut(block: (ptr: CPointer<UIntVar>) -> Unit): UInt {
    block(uint1Ptr)
    return uint1.value
}

inline fun IByteData.ptr() = (this as NativeByteData).posPtr
inline fun IByteData.ubytePtr() = (this as NativeByteData).posPtr.reinterpret<UByteVar>()
inline fun IShortData.ptr() = (this as NativeShortData).posPtr
inline fun IFloatData.ptr() = (this as NativeFloatData).posPtr
inline fun IIntData.ptr() = (this as NativeIntData).posPtr
inline fun IIntData.uintPtr() = (this as NativeIntData).posPtr.reinterpret<UIntVar>()

inline fun NativePlacement.allocInt() = alloc<IntVar>()

inline fun IntArray.readUInts(context: (pointer: CArrayPointer<UIntVar>) -> Unit) {
    memScoped {
        val ints = allocArray<UIntVar>(size)
        context(ints)
        for (i in 0 until size) {
            set(i, ints[i].toInt())
        }
    }
}
