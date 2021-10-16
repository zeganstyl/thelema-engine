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

package app.thelema.audio

import app.thelema.data.IByteData
import app.thelema.fs.IFile

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
        file.readBytes(
            error = { throw IllegalStateException("Can't read file ${file.path}, status: $it") },
            ready = { bytes ->
                if (bytes.get().toInt().toChar() != 'R' || bytes.get().toInt().toChar() != 'I' || bytes.get().toInt().toChar() != 'F' || bytes.get().toInt().toChar() != 'F') throw RuntimeException("RIFF header not found: $file")
                bytes.position += 4
                if (bytes.get().toInt().toChar() != 'W' || bytes.get().toInt().toChar() != 'A' || bytes.get().toInt().toChar() != 'V' || bytes.get().toInt().toChar() != 'E') throw RuntimeException("Invalid wave file header: $file")

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
                bytes.limit = seekToChunk(bytes, 'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte())
            }
        )
    }
}