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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.net.NET
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

/** STB Vorbis implementation
 *
 * @author zeganstyl */
class OggMusic(audio: OpenAL, file: IFile) : OpenALMusic(audio, file) {
    private var encodedAudio: IByteData? = null

    private val sampleIndex = AtomicInteger()

    private var handle: Long = -1

    init {
        if (!audio.noDevice) {
            file.readByteData { status, data ->
                if (status == NET.OK) {
                    encodedAudio = data
                    val stack = MemoryStack.stackPush()
                    val error = stack.mallocInt(1)
                    handle = STBVorbis.stb_vorbis_open_memory(data.sourceObject as ByteBuffer, error, null)
                    if (handle == MemoryUtil.NULL) {
                        throw RuntimeException("Failed to open Ogg Vorbis file. Error: " + error[0])
                    }
                    val info = STBVorbisInfo.mallocStack(stack)
                    setup(info.channels(), info.sample_rate())

                    sampleIndex.set(0)
                } else {
                    throw IllegalArgumentException("Error reading file, status: $status")
                }
            }
        }
    }

    fun setSampleIndex(sampleIndex: Int) {
        this.sampleIndex.set(sampleIndex)
    }

    @Synchronized
    private fun seek(sampleIndex: Int) {
        STBVorbis.stb_vorbis_seek(handle, sampleIndex)
        setSampleIndex(sampleIndex)
    }

    override fun read(buffer: ByteArray): Int {
        val bytesPerCh = STBVorbis.stb_vorbis_get_samples_short_interleaved(handle, channels, shortBuffer) * 2

        var samples = 0
        for (i in 0 until channels) {
            samples += bytesPerCh
        }

        for (i in 0 until samples) {
            buffer[i] = byteBuffer[i]
        }

        return samples
    }

    override fun reset() {
        seek(0)
    }

    override fun loop() {
        seek(0)
    }

    companion object {
        private val byteBuffer = BufferUtils.createByteBuffer(bufferSize)
        private val shortBuffer = byteBuffer.asShortBuffer()
    }
}
