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

import java.io.*


/** Taken from http://www.javafaq.nu/java-example-code-1079.html
 * @author mzechner
 */
class LittleEndianInputStream(`in`: InputStream) : FilterInputStream(`in`), DataInput {
    private val din: DataInputStream = DataInputStream(`in`)

    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        din.readFully(b)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray, off: Int, len: Int) {
        din.readFully(b, off, len)
    }

    @Throws(IOException::class)
    override fun skipBytes(n: Int): Int {
        return din.skipBytes(n)
    }

    @Throws(IOException::class)
    override fun readBoolean(): Boolean {
        return din.readBoolean()
    }

    @Throws(IOException::class)
    override fun readByte(): Byte {
        return din.readByte()
    }

    @Throws(IOException::class)
    override fun readUnsignedByte(): Int {
        return din.readUnsignedByte()
    }

    @Throws(IOException::class)
    override fun readShort(): Short {
        val low = din.read()
        val high = din.read()
        return (high shl 8 or (low and 0xff)).toShort()
    }

    @Throws(IOException::class)
    override fun readUnsignedShort(): Int {
        val low = din.read()
        val high = din.read()
        return high and 0xff shl 8 or (low and 0xff)
    }

    @Throws(IOException::class)
    override fun readChar(): Char {
        return din.readChar()
    }

    @Throws(IOException::class)
    override fun readInt(): Int {
        val res = IntArray(4)
        for (i in 3 downTo 0) res[i] = din.read()
        return res[0] and 0xff shl 24 or (res[1] and 0xff shl 16) or (res[2] and 0xff shl 8) or (res[3] and 0xff)
    }

    @Throws(IOException::class)
    override fun readLong(): Long {
        val res = IntArray(8)
        for (i in 7 downTo 0) res[i] = din.read()
        return ((res[0] and 0xff).toLong() shl 56 or ((res[1] and 0xff).toLong() shl 48) or ((res[2] and 0xff).toLong() shl 40)
                or ((res[3] and 0xff).toLong() shl 32) or ((res[4] and 0xff).toLong() shl 24) or ((res[5] and 0xff).toLong() shl 16)
                or ((res[6] and 0xff).toLong() shl 8) or (res[7] and 0xff).toLong())
    }

    @Throws(IOException::class)
    override fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    @Throws(IOException::class)
    override fun readLine(): String {
        return din.readLine()
    }

    @Throws(IOException::class)
    override fun readUTF(): String {
        return din.readUTF()
    }

}
