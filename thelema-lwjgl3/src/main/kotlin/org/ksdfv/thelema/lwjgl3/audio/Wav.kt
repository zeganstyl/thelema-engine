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

package org.ksdfv.thelema.lwjgl3.audio

import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.StreamUtils
import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException

class Wav {
    class Music(audio: OpenAL, file: IFile) : OpenALMusic(audio, file) {
        private var input: WavInputStream?
        override fun read(buffer: ByteArray): Int {
            if (input == null) {
                input = WavInputStream(file)
                setup(input!!.channels, input!!.sampleRate)
            }
            return try {
                input!!.read(buffer)
            } catch (ex: IOException) {
                throw RuntimeException("Error reading WAV file: $file", ex)
            }
        }

        override fun reset() {
            StreamUtils.closeQuietly(input)
            input = null
        }

        init {
            input = WavInputStream(file)
            if (!audio.noDevice) {
                setup(input!!.channels, input!!.sampleRate)
            }
        }
    }

    class Sound(audio: OpenAL, file: IFile) : OpenALSound(audio) {
        init {
            if (!audio.noDevice) {
                var input: WavInputStream? = null
                try {
                    input = WavInputStream(file)
                    setup(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate)
                } catch (ex: IOException) {
                    throw RuntimeException("Error reading WAV file: $file", ex)
                } finally {
                    StreamUtils.closeQuietly(input)
                }
            }
        }
    }

    /** @author Nathan Sweet
     */
    private class WavInputStream internal constructor(file: IFile) : FilterInputStream(file.read()) {
        var channels = 0
        var sampleRate = 0
        var dataRemaining = 0
        @Throws(IOException::class)
        private fun seekToChunk(c1: Char, c2: Char, c3: Char, c4: Char): Int {
            while (true) {
                var found = read() == c1.toInt()
                found = found and (read() == c2.toInt())
                found = found and (read() == c3.toInt())
                found = found and (read() == c4.toInt())
                val chunkLength = read() and 0xff or (read() and 0xff shl 8) or (read() and 0xff shl 16) or (read() and 0xff shl 24)
                if (chunkLength == -1) throw IOException("Chunk not found: $c1$c2$c3$c4")
                if (found) return chunkLength
                skipFully(chunkLength)
            }
        }

        @Throws(IOException::class)
        private fun skipFully(count: Int) {
            var count = count
            while (count > 0) {
                val skipped = `in`.skip(count.toLong())
                if (skipped <= 0) throw EOFException("Unable to skip.")
                count -= skipped.toInt()
            }
        }

        @Throws(IOException::class)
        override fun read(buffer: ByteArray): Int {
            if (dataRemaining == 0) return -1
            var offset = 0
            do {
                val length = Math.min(super.read(buffer, offset, buffer.size - offset), dataRemaining)
                if (length == -1) {
                    return if (offset > 0) offset else -1
                }
                offset += length
                dataRemaining -= length
            } while (offset < buffer.size)
            return offset
        }

        init {
            try {
                if (read() != 'R'.toInt() || read() != 'I'.toInt() || read() != 'F'.toInt() || read() != 'F'.toInt()) throw RuntimeException("RIFF header not found: $file")
                skipFully(4)
                if (read() != 'W'.toInt() || read() != 'A'.toInt() || read() != 'V'.toInt() || read() != 'E'.toInt()) throw RuntimeException("Invalid wave file header: $file")
                val fmtChunkLength = seekToChunk('f', 'm', 't', ' ')
                val type = (read() and 0xff) or ((read() and 0xff) shl 8)
                if (type != 1) throw RuntimeException("WAV files must be PCM: $type")
                channels = (read() and 0xff) or ((read() and 0xff) shl 8)
                if (channels != 1 && channels != 2) throw RuntimeException("WAV files must have 1 or 2 channels: $channels")
                sampleRate = (read() and 0xff) or (read() and 0xff shl 8) or (read() and 0xff shl 16) or (read() and 0xff shl 24)
                skipFully(6)
                val bitsPerSample = (read() and 0xff) or ((read() and 0xff) shl 8)
                if (bitsPerSample != 16) throw RuntimeException("WAV files must have 16 bits per sample: $bitsPerSample")
                skipFully(fmtChunkLength - 16)
                dataRemaining = seekToChunk('d', 'a', 't', 'a')
            } catch (ex: Throwable) {
                StreamUtils.closeQuietly(this)
                throw RuntimeException("Error reading WAV file: $file", ex)
            }
        }
    }
}
