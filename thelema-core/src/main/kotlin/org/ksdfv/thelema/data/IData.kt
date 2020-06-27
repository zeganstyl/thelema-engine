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

package org.ksdfv.thelema.data

/** @author zeganstyl */
interface IData {
    /** A null buffer can be used to replace old buffers in variables so
     * that the garbage collector can delete the old buffer.
     * But you must remove all references everywhere to that buffer for deleting him. */
    val nullBuffer: IByteData

    fun decodeURI(uri: String): String
    fun encodeURI(uri: String): String

    fun decodeBase64(text: String): ByteArray
    fun decodeBase64(text: String, out: IByteData): IByteData
    fun encodeBase64(bytes: ByteArray): String
    fun encodeBase64(data: IByteData): String

    /** Platform depended data (buffer or array) allocation */
    fun bytes(capacity: Int): IByteData

    /** If data needs to be destroy, destroy it through this method */
    fun destroyBytes(data: IByteData)
}