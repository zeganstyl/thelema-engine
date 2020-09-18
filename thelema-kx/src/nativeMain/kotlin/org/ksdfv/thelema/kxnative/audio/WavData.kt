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

package org.ksdfv.thelema.kxnative.audio

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.net.NET

/** @author Nathan Sweet
 */
class WavData(file: IFile) {
    var channels = 0
    var sampleRate = 0

    private fun seekToChunk(bytes: IByteData, c1: Byte, c2: Byte, c3: Byte, c4: Byte): Int {
        while (true) {
            var found = bytes.get() == c1
            found = found and (bytes.get() == c2)
            found = found and (bytes.get() == c3)
            found = found and (bytes.get() == c4)
            val chunkLength = bytes.get().toInt() and 0xff or (bytes.get().toInt() and 0xff shl 8) or (bytes.get().toInt() and 0xff shl 16) or (bytes.get().toInt() and 0xff shl 24)
            if (chunkLength == -1) throw IllegalStateException("Chunk not found: $c1$c2$c3$c4")
            if (found) return chunkLength
            bytes.position += chunkLength
        }
    }

    var bytes: IByteData? = null

    init {
        file.readByteData { status, bytes ->
            this.bytes = bytes
            if (status == NET.OK) {
                if (bytes.get().toChar() != 'R' || bytes.get().toChar() != 'I' || bytes.get().toChar() != 'F' || bytes.get().toChar() != 'F') throw RuntimeException("RIFF header not found: $file")
                bytes.position += 4
                if (bytes.get().toChar() != 'W' || bytes.get().toChar() != 'A' || bytes.get().toChar() != 'V' || bytes.get().toChar() != 'E') throw RuntimeException("Invalid wave file header: $file")

                val fmtChunkLength = seekToChunk(bytes, 'f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte())
                val type = (bytes.get().toInt() and 0xff) or ((bytes.get().toInt() and 0xff) shl 8)
                if (type != 1) throw RuntimeException("WAV files must be PCM: $type")
                channels = (bytes.get().toInt() and 0xff) or ((bytes.get().toInt() and 0xff) shl 8)
                if (channels != 1 && channels != 2) throw RuntimeException("WAV files must have 1 or 2 channels: $channels")
                sampleRate = (bytes.get().toInt() and 0xff) or (bytes.get().toInt() and 0xff shl 8) or (bytes.get().toInt() and 0xff shl 16) or (bytes.get().toInt() and 0xff shl 24)
                bytes.position += 6
                val bitsPerSample = (bytes.get().toInt() and 0xff) or ((bytes.get().toInt() and 0xff) shl 8)
                if (bitsPerSample != 16) throw RuntimeException("WAV files must have 16 bits per sample: $bitsPerSample")
                bytes.position += fmtChunkLength - 16
                bytes.size = seekToChunk(bytes, 'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte())
            } else {
                throw IllegalStateException("Can't read file ${file.path}, status: $status")
            }
        }
    }
}