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

package app.thelema.data

/** @author zeganstyl */
interface IData {
    /** A null buffer can be used to replace old buffers in variables so
     * that the garbage collector can delete the old buffer.
     * But you must remove all references everywhere to that buffer for deleting him. */
    val nullBuffer: IByteData

    fun decodeURI(uri: String): String  {
        var needToChange = false
        val numChars = uri.length
        val sb = StringBuilder(if (numChars > 500) numChars / 2 else numChars)
        var i = 0
        var c: Char
        var bytes: CharArray? = null
        while (i < numChars) {
            c = uri[i]
            when (c) {
                '+' -> {
                    sb.append(' ')
                    i++
                    needToChange = true
                }
                '%' -> {
                    /*
                     * Starting with this instance of %, process all
                     * consecutive substrings of the form %xy. Each
                     * substring %xy will yield a byte. Convert all
                     * consecutive  bytes obtained this way to whatever
                     * character(s) they represent in the provided
                     * encoding.
                     */try {

                        // (numChars-i)/3 is an upper bound for the number
                        // of remaining bytes
                        if (bytes == null) bytes = CharArray((numChars - i) / 3)
                        var pos = 0
                        while (i + 2 < numChars &&
                            c == '%'
                        ) {
                            val v = uri.substring(i + 1, i + 3).toInt(16)
                            if (v < 0) throw IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value")
                            bytes[pos++] = v.toChar()
                            i += 3
                            if (i < numChars) c = uri[i]
                        }

                        // A trailing, incomplete byte encoding such as
                        // "%x" will cause an exception to be thrown
                        if (i < numChars && c == '%') throw IllegalArgumentException(
                            "URLDecoder: Incomplete trailing escape (%) pattern"
                        )
                        sb.append(bytes.concatToString(0, pos))
                    } catch (e: NumberFormatException) {
                        throw IllegalArgumentException(
                            "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                    + e.message
                        )
                    }
                    needToChange = true
                }
                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        return if (needToChange) sb.toString() else uri
    }

    fun encodeURI(uri: String): String

    fun decodeBase64(text: String): ByteArray
    fun decodeBase64(text: String, out: IByteData): IByteData
    fun encodeBase64(bytes: ByteArray): String
    fun encodeBase64(data: IByteData): String

    /** Platform depended data (buffer or array) allocation */
    fun bytes(capacity: Int): IByteData

    /** Create byte buffer from string */
    fun bytes(text: String): IByteData

    fun bytes(array: ByteArray): IByteData = DATA.bytes(array.size).apply {
        put(array)
        rewind()
    }

    /** If data needs to be destroy, destroy it through this method */
    fun destroyBytes(data: IByteData)
}