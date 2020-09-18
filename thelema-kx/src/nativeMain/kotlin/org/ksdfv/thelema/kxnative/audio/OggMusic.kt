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

import glfw.stb_vorbis
import glfw.stb_vorbis_info
import kotlinx.cinterop.*
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.kxnative.ptr
import org.ksdfv.thelema.kxnative.ubytePtr
import org.ksdfv.thelema.kxnative.uint
import org.ksdfv.thelema.net.NET

/** STB Vorbis implementation
 *
 * @author zeganstyl */
class OggMusic(val audio: OpenAL, file: IFile) : OpenALMusic(audio, file) {
    private var encodedAudio: IByteData? = null

    private var sampleIndex = 0

    private var handle: CPointer<stb_vorbis>? = null

    init {
        if (!audio.noDevice) {
            file.readByteData { status, data ->
                if (status == NET.OK) {
                    encodedAudio = data

                    memScoped {
                        val error = alloc<IntVar>()
                        handle = glfw.stb_vorbis_open_memory(data.ubytePtr(), data.size, error.ptr, null)
                        if (handle == null) {
                            throw RuntimeException("Failed to open Ogg Vorbis file. Error: ${error.value}")
                        }

                        val vorbisInfo = alloc<stb_vorbis_info>()

                        setup(vorbisInfo.channels, vorbisInfo.sample_rate.toInt())
                    }

                    sampleIndex = 0
                } else {
                    throw IllegalArgumentException("Error reading file, status: $status")
                }
            }
        }
    }

    fun setSampleIndex(sampleIndex: Int) {
        this.sampleIndex = sampleIndex
    }

    private fun seek(sampleIndex: Int) {
        glfw.stb_vorbis_seek(handle, sampleIndex.uint())
        setSampleIndex(sampleIndex)
    }

    override fun read(buffer: ByteArray): Int {
        val bytesPerCh = glfw.stb_vorbis_get_samples_short_interleaved(handle, channels, audio.shortBuffer.ptr(), audio.shortBuffer.size) * 2

        val samples = bytesPerCh * channels

        for (i in 0 until samples) {
            buffer[i] = audio.tempBuffer[i]
        }

        return samples
    }

    override fun reset() {
        seek(0)
    }

    override fun loop() {
        seek(0)
    }
}
