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

import org.ksdfv.thelema.audio.IAudioDevice
import org.ksdfv.thelema.math.MATH
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.experimental.and
import kotlin.math.min


/** @author Nathan Sweet
 */
class OpenALAudioDevice(
    private val audio: OpenAL,
    sampleRate: Int,
    override val channelsNum: Int,
    private val bufferSize: Int,
    private val bufferCount: Int
) : IAudioDevice {
    private var buffers: IntBuffer? = null
    private var sourceID = -1
    private val format: Int
    val rate: Int
    private var isPlaying = false
    private var volume = 1f
    private var renderedSeconds = 0f
    private val secondsPerBuffer: Float
    private var bytes: ByteArray? = null
    private val tempBuffer: ByteBuffer

    override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {
        if (bytes == null || bytes!!.size < numSamples * 2) bytes = ByteArray(numSamples * 2)
        val end = min(offset + numSamples, samples.size)
        var i = offset
        var ii = 0
        while (i < end) {
            val sample = samples[i]

            bytes!![ii++] = (sample and 0xFF).toByte()
            bytes!![ii++] = (sample.toInt() shr 8 and 0xFF).toByte()
            i++
        }
        writeSamples(bytes!!, 0, numSamples * 2)
    }

    override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {
        if (bytes == null || bytes!!.size < numSamples * 2) bytes = ByteArray(numSamples * 2)
        val end = min(offset + numSamples, samples.size)
        var i = offset
        var ii = 0
        while (i < end) {
            var floatSample = samples[i]
            floatSample = MATH.clamp(floatSample, -1f, 1f)
            val intSample = (floatSample * 32767).toInt()
            bytes!![ii++] = (intSample and 0xFF).toByte()
            bytes!![ii++] = (intSample shr 8 and 0xFF).toByte()
            i++
        }
        writeSamples(bytes!!, 0, numSamples * 2)
    }

    fun writeSamples(data: ByteArray, offset: Int, length: Int) {
        var offset = offset
        var length = length
        require(length >= 0) { "length cannot be < 0." }
        if (sourceID == -1) {
            sourceID = audio.obtainSource(true)
            if (sourceID == -1) return
            if (buffers == null) {
                buffers = BufferUtils.createIntBuffer(bufferCount)
                AL10.alGenBuffers(buffers)
                if (AL10.alGetError() != AL10.AL_NO_ERROR) throw RuntimeException("Unabe to allocate audio buffers.")
            }
            AL10.alSourcei(sourceID, AL10.AL_LOOPING, AL10.AL_FALSE)
            AL10.alSourcef(sourceID, AL10.AL_GAIN, volume)
            // Fill initial buffers.
            var queuedBuffers = 0
            for (i in 0 until bufferCount) {
                val bufferID = buffers!![i]
                val written = min(bufferSize, length)
                tempBuffer.clear()
                tempBuffer.put(data, offset, written).flip()
                AL10.alBufferData(bufferID, format, tempBuffer, rate)
                AL10.alSourceQueueBuffers(sourceID, bufferID)
                length -= written
                offset += written
                queuedBuffers++
            }
            // Queue rest of buffers, empty.
            tempBuffer.clear().flip()
            for (i in queuedBuffers until bufferCount) {
                val bufferID = buffers!![i]
                AL10.alBufferData(bufferID, format, tempBuffer, rate)
                AL10.alSourceQueueBuffers(sourceID, bufferID)
            }
            AL10.alSourcePlay(sourceID)
            isPlaying = true
        }
        while (length > 0) {
            val written = fillBuffer(data, offset, length)
            length -= written
            offset += written
        }
    }

    /** Blocks until some of the data could be buffered.  */
    private fun fillBuffer(data: ByteArray, offset: Int, length: Int): Int {
        val written = min(bufferSize, length)
        outer@ while (true) {
            var buffers = AL10.alGetSourcei(sourceID, AL10.AL_BUFFERS_PROCESSED)
            while (buffers-- > 0) {
                val bufferID = AL10.alSourceUnqueueBuffers(sourceID)
                if (bufferID == AL10.AL_INVALID_VALUE) break
                renderedSeconds += secondsPerBuffer
                tempBuffer.clear()
                tempBuffer.put(data, offset, written).flip()
                AL10.alBufferData(bufferID, format, tempBuffer, rate)
                AL10.alSourceQueueBuffers(sourceID, bufferID)
                break@outer
            }
            // Wait for buffer to be free.
            try {
                Thread.sleep((1000 * secondsPerBuffer).toLong())
            } catch (ignored: InterruptedException) {
            }
        }
        // A buffer underflow will cause the source to stop.
        if (!isPlaying || AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
            AL10.alSourcePlay(sourceID)
            isPlaying = true
        }
        return written
    }

    fun stop() {
        if (sourceID == -1) return
        audio.freeSource(sourceID)
        sourceID = -1
        renderedSeconds = 0f
        isPlaying = false
    }

    fun isPlaying(): Boolean {
        return if (sourceID == -1) false else isPlaying
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        if (sourceID != -1) AL10.alSourcef(sourceID, AL10.AL_GAIN, volume)
    }

    var position: Float
        get() = if (sourceID == -1) 0f else renderedSeconds + AL10.alGetSourcef(sourceID, AL11.AL_SEC_OFFSET)
        set(position) {
            renderedSeconds = position
        }

    fun getChannels(): Int {
        return if (format == AL10.AL_FORMAT_STEREO16) 2 else 1
    }

    override fun destroy() {
        if (buffers == null) return
        if (sourceID != -1) {
            audio.freeSource(sourceID)
            sourceID = -1
        }
        AL10.alDeleteBuffers(buffers)
        buffers = null
    }

    companion object {
        private const val bytesPerSample = 2
    }

    init {
        format = if (channelsNum > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16
        rate = sampleRate
        secondsPerBuffer = bufferSize.toFloat() / bytesPerSample / channelsNum / sampleRate
        tempBuffer = BufferUtils.createByteBuffer(bufferSize)
    }
}
