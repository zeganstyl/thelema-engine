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

package org.ksdfv.thelema

import java.io.*
import kotlin.math.max

/** Provides utility methods to copy streams.  */
object StreamUtils {
    const val DEFAULT_BUFFER_SIZE = 4096
    val EMPTY_BYTES = ByteArray(0)
    /** Copy the data from an [InputStream] to an [OutputStream], using the specified byte[] as a temporary buffer. The
     * stream is not closed.  */
    /** Allocates a {@value #DEFAULT_BUFFER_SIZE} byte[] for use as a temporary buffer and calls
     * [.copyStream].  */
    @Throws(IOException::class)
    fun copyStream(input: InputStream, output: OutputStream, buffer: ByteArray = ByteArray(DEFAULT_BUFFER_SIZE)) {
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
    /** Copy the data from an [InputStream] to a byte array. The stream is not closed.
     * @param estimatedSize Used to allocate the output byte[] to possibly avoid an array copy.
     */
    /** Copy the data from an [InputStream] to a byte array. The stream is not closed.  */
    @Throws(IOException::class)
    fun copyStreamToByteArray(input: InputStream, estimatedSize: Int = input.available()): ByteArray {
        val baos: ByteArrayOutputStream = OptimizedByteArrayOutputStream(max(0, estimatedSize))
        copyStream(input, baos)
        BufferedInputStream(input)
        return baos.toByteArray()
    }
    /** Copy the data from an [InputStream] to a string using the specified charset.
     * @param estimatedSize Used to allocate the output buffer to possibly avoid an array copy.
     * @param charset May be null to use the platform's default charset.
     */
    /** Calls [.copyStreamToString] using the input's [available][InputStream.available] size
     * and the platform's default charset.  */
    /** Calls [.copyStreamToString] using the platform's default charset.  */
    @Throws(IOException::class)
    fun copyStreamToString(input: InputStream, estimatedSize: Int = input.available(), charset: String? = null): String {
        val reader = charset?.let { InputStreamReader(input, it) } ?: InputStreamReader(input)
        val writer = StringWriter(max(0, estimatedSize))
        val buffer = CharArray(DEFAULT_BUFFER_SIZE)

        var charsRead: Int = reader.read(buffer)
        while (charsRead != -1) {
            writer.write(buffer, 0, charsRead)
            charsRead = reader.read(buffer)
        }
        return writer.toString()
    }

    /** Close and ignore all errors.  */
    fun closeQuietly(c: Closeable?) {
        if (c != null) {
            try {
                c.close()
            } catch (ignored: Throwable) {
            }
        }
    }

    /** A ByteArrayOutputStream which avoids copying of the byte array if possible.  */
    class OptimizedByteArrayOutputStream(initialSize: Int) : ByteArrayOutputStream(initialSize) {
        @Synchronized
        override fun toByteArray(): ByteArray {
            return if (count == buf.size) buf else super.toByteArray()
        }

        val buffer: ByteArray
            get() = buf
    }
}
