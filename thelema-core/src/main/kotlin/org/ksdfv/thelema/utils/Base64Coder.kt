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

package org.ksdfv.thelema.utils

import kotlin.math.min

object Base64Coder {
    // The line separator string of the operating system.
    private const val systemLineSeparator = "\n"
    val regularMap = CharMap('+', '/')
    val urlsafeMap = CharMap('-', '_')
    /** Encodes a string into Base64 format. No blanks or line breaks are inserted.
     * @param s A String to be encoded.
     * @return A String containing the Base64 encoded data.
     */
    @JvmOverloads
    fun encodeString(s: String, useUrlsafeEncoding: Boolean = false): String {
        return String(
            encode(
                s.toByteArray(),
                if (useUrlsafeEncoding) urlsafeMap.encodingMap else regularMap.encodingMap
            )
        )
    }

    fun encodeLines(`in`: ByteArray, iOff: Int, iLen: Int, lineLen: Int, lineSeparator: String, charMap: CharMap): String {
        return encodeLines(
            `in`,
            iOff,
            iLen,
            lineLen,
            lineSeparator,
            charMap.encodingMap
        )
    }
    /** Encodes a byte array into Base 64 format and breaks the output into lines.
     * @param in An array containing the data bytes to be encoded.
     * @param iOff Offset of the first byte in `in` to be processed.
     * @param iLen Number of bytes to be processed in `in`, starting at `iOff`.
     * @param lineLen Line length for the output data. Should be a multiple of 4.
     * @param lineSeparator The line separator to be used to separate the output lines.
     * @param charMap char map to use
     * @return A String containing the Base64 encoded data, broken into lines.
     */
    @JvmOverloads
    fun encodeLines(
        `in`: ByteArray,
        iOff: Int = 0,
        iLen: Int = `in`.size,
        lineLen: Int = 76,
        lineSeparator: String = systemLineSeparator,
        charMap: CharArray = regularMap.encodingMap
    ): String {
        val blockLen = lineLen * 3 / 4
        require(blockLen > 0)
        val lines = (iLen + blockLen - 1) / blockLen
        val bufLen = (iLen + 2) / 3 * 4 + lines * lineSeparator.length
        val buf = StringBuilder(bufLen)
        var ip = 0
        while (ip < iLen) {
            val l = min(iLen - ip, blockLen)
            buf.append(encode(`in`, iOff + ip, l, charMap))
            buf.append(lineSeparator)
            ip += l
        }
        return buf.toString()
    }

    fun encode(`in`: ByteArray, charMap: CharMap): CharArray {
        return encode(`in`, 0, `in`.size, charMap)
    }

    /** Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
     * @param in An array containing the data bytes to be encoded.
     * @return A character array containing the Base64 encoded data.
     */
    @JvmOverloads
    fun encode(`in`: ByteArray, charMap: CharArray = regularMap.encodingMap): CharArray {
        return encode(`in`, 0, `in`.size, charMap)
    }

    /** Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
     * @param in An array containing the data bytes to be encoded.
     * @param iLen Number of bytes to process in `in`.
     * @return A character array containing the Base64 encoded data.
     */
    fun encode(`in`: ByteArray, iLen: Int): CharArray {
        return encode(
            `in`,
            0,
            iLen,
            regularMap.encodingMap
        )
    }

    fun encode(`in`: ByteArray, iOff: Int, iLen: Int, charMap: CharMap): CharArray {
        return encode(`in`, iOff, iLen, charMap.encodingMap)
    }

    /** Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
     * @param in An array containing the data bytes to be encoded.
     * @param iOff Offset of the first byte in `in` to be processed.
     * @param iLen Number of bytes to process in `in`, starting at `iOff`.
     * @param charMap char map to use
     * @return A character array containing the Base64 encoded data.
     */
    fun encode(`in`: ByteArray, iOff: Int, iLen: Int, charMap: CharArray): CharArray {
        val oDataLen = (iLen * 4 + 2) / 3 // output length without padding
        val oLen = (iLen + 2) / 3 * 4 // output length including padding
        val out = CharArray(oLen)
        var ip = iOff
        val iEnd = iOff + iLen
        var op = 0
        while (ip < iEnd) {
            val i0: Int = `in`[ip++].toInt() and 0xff
            val i1 = if (ip < iEnd) `in`[ip++].toInt() and 0xff else 0
            val i2 = if (ip < iEnd) `in`[ip++].toInt() and 0xff else 0
            val o0 = i0 ushr 2
            val o1 = i0 and 3 shl 4 or (i1 ushr 4)
            val o2 = i1 and 0xf shl 2 or (i2 ushr 6)
            val o3 = i2 and 0x3F
            out[op++] = charMap[o0]
            out[op++] = charMap[o1]
            out[op] = if (op < oDataLen) charMap[o2] else '='
            op++
            out[op] = if (op < oDataLen) charMap[o3] else '='
            op++
        }
        return out
    }

    /** Decodes a string from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input data.
     * @param s A Base64 String to be decoded.
     * @return A String containing the decoded data.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    @JvmOverloads
    fun decodeString(s: String, useUrlSafeEncoding: Boolean = false): String {
        return String(
            decode(
                s.toCharArray(),
                if (useUrlSafeEncoding) urlsafeMap.decodingMap else regularMap.decodingMap
            )
        )
    }

    fun decodeLines(s: String, inverseCharMap: CharMap): ByteArray {
        return decodeLines(s, inverseCharMap.decodingMap)
    }

    /** Decodes a byte array from Base64 format and ignores line separators, tabs and blanks. CR, LF, Tab and Space characters are
     * ignored in the input data. This method is compatible with `sun.misc.BASE64Decoder.decodeBuffer(String)`.
     * @param s A Base64 String to be decoded.
     * @param inverseCharMap
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    @JvmOverloads
    fun decodeLines(s: String, inverseCharMap: ByteArray = regularMap.decodingMap): ByteArray {
        val buf = CharArray(s.length)
        var p = 0
        for (ip in s.indices) {
            val c = s[ip]
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                buf[p++] = c
            }
        }
        return decode(buf, 0, p, inverseCharMap)
    }

    /** Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input data.
     * @param s A Base64 String to be decoded.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    fun decode(s: String): ByteArray {
        return decode(s.toCharArray())
    }

    /** Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input data.
     * @param s A Base64 String to be decoded.
     * @param inverseCharMap
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    fun decode(s: String, inverseCharMap: CharMap?): ByteArray {
        return decode(s.toCharArray(), inverseCharMap!!)
    }

    fun decode(`in`: CharArray, inverseCharMap: ByteArray): ByteArray {
        return decode(`in`, 0, `in`.size, inverseCharMap)
    }

    fun decode(`in`: CharArray, inverseCharMap: CharMap): ByteArray {
        return decode(`in`, 0, `in`.size, inverseCharMap)
    }

    fun decode(`in`: CharArray, iOff: Int, iLen: Int, inverseCharMap: CharMap): ByteArray {
        return decode(
            `in`,
            iOff,
            iLen,
            inverseCharMap.decodingMap
        )
    }
    /** Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64 encoded input data.
     * @param in A character array containing the Base64 encoded data.
     * @param iOff Offset of the first character in `in` to be processed.
     * @param iLen Number of characters to process in `in`, starting at `iOff`.
     * @param inverseCharMap charMap to use
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
     */
    @JvmOverloads
    fun decode(
        `in`: CharArray,
        iOff: Int = 0,
        iLen: Int = `in`.size,
        inverseCharMap: ByteArray = regularMap.decodingMap
    ): ByteArray {
        var iLen = iLen
        require(iLen % 4 == 0) { "Length of Base64 encoded input string is not a multiple of 4." }
        while (iLen > 0 && `in`[iOff + iLen - 1] == '=') {
            iLen--
        }
        val oLen = iLen * 3 / 4
        val out = ByteArray(oLen)
        var ip = iOff
        val iEnd = iOff + iLen
        var op = 0
        while (ip < iEnd) {
            val i0 = `in`[ip++].toInt()
            val i1 = `in`[ip++].toInt()
            val i2 = if (ip < iEnd) `in`[ip++].toInt() else 'A'.toInt()
            val i3 = if (ip < iEnd) `in`[ip++].toInt() else 'A'.toInt()
            require(!(i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)) { "Illegal character in Base64 encoded data." }
            val b0 = inverseCharMap[i0].toInt()
            val b1 = inverseCharMap[i1].toInt()
            val b2 = inverseCharMap[i2].toInt()
            val b3 = inverseCharMap[i3].toInt()
            require(!(b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)) { "Illegal character in Base64 encoded data." }
            val o0 = b0 shl 2 or (b1 ushr 4)
            val o1 = b1 and 0xf shl 4 or (b2 ushr 2)
            val o2 = b2 and 3 shl 6 or b3
            out[op++] = o0.toByte()
            if (op < oLen) {
                out[op++] = o1.toByte()
            }
            if (op < oLen) {
                out[op++] = o2.toByte()
            }
        }
        return out
    }

    class CharMap(char63: Char, char64: Char) {
        val encodingMap = CharArray(64)
        val decodingMap = ByteArray(128)

        init {
            var i = 0
            run {
                var c = 'A'
                while (c <= 'Z') {
                    encodingMap[i++] = c
                    c++
                }
            }
            run {
                var c = 'a'
                while (c <= 'z') {
                    encodingMap[i++] = c
                    c++
                }
            }
            var c = '0'
            while (c <= '9') {
                encodingMap[i++] = c
                c++
            }
            encodingMap[i++] = char63
            encodingMap[i++] = char64
            i = 0
            while (i < decodingMap.size) {
                decodingMap[i] = -1
                i++
            }
            i = 0
            while (i < 64) {
                decodingMap[encodingMap[i].toInt()] = i.toByte()
                i++
            }
        }
    }
}
