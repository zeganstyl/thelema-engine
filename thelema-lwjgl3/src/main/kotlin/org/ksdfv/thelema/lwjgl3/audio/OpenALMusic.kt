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

import org.ksdfv.thelema.audio.IMusic
import org.ksdfv.thelema.fs.IFile
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import org.lwjgl.openal.SOFTDirectChannels
import java.nio.IntBuffer
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


/** @author Nathan Sweet
 */
abstract class OpenALMusic(private val audio: OpenAL, protected val file: IFile) : IMusic {
    private val renderedSecondsQueue = Stack<Float>()
    private var buffers: IntBuffer? = null
    var sourceId = -1
        private set
    private var format = 0
    var rate = 0
        private set
    override var isLooping = false
    override var isPlaying = false
        get() {
            if (audio.noDevice) return false
            return if (sourceId == -1) false else field
        }
        set(value) {
            field = value
            if (value) {
                removeRequest = false
            }
        }
    override var volume = 1f
        set(value) {
            field = value
            if (audio.noDevice) return
            if (sourceId != -1) AL10.alSourcef(sourceId, AL10.AL_GAIN, value)
        }
    private var pan = 0f
    private var renderedSeconds = 0f
    private var maxSecondsPerBuffer = 0f
    override var onCompletionListener: IMusic.OnCompletionListener? = null

    var removeRequest = false
        set(value) {
            field = value
            if (value) audio.checkAndValidatePlayingMusic = true
        }

    protected fun setup(channels: Int, sampleRate: Int) {
        format = if (channels > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16
        rate = sampleRate
        maxSecondsPerBuffer = bufferSize.toFloat() / (bytesPerSample * channels * sampleRate)
    }

    override fun play() {
        if (audio.noDevice) return
        if (sourceId == -1) {
            sourceId = audio.obtainSource(true)
            if (sourceId == -1) return
            audio.music.add(this)
            if (buffers == null) {
                buffers = BufferUtils.createIntBuffer(bufferCount)
                AL10.alGenBuffers(buffers)
                val errorCode = AL10.alGetError()
                if (errorCode != AL10.AL_NO_ERROR) throw RuntimeException("Unable to allocate audio buffers. AL Error: $errorCode")
            }
            AL10.alSourcei(sourceId, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, AL10.AL_TRUE)
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_FALSE)
            setPan(pan)
            var filled = false // Check if there's anything to actually play.
            for (i in 0 until bufferCount) {
                val bufferID = buffers!![i]
                if (!fill(bufferID)) break
                filled = true
                AL10.alSourceQueueBuffers(sourceId, bufferID)
            }
            if (!filled && onCompletionListener != null) onCompletionListener!!.onCompletion(this)
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                stop()
                return
            }
        }
        if (!isPlaying) {
            AL10.alSourcePlay(sourceId)
            isPlaying = true
        }
    }

    override fun stop() {
        if (audio.noDevice) return
        if (sourceId == -1) return
        reset()
        audio.freeSource(sourceId)
        sourceId = -1
        renderedSeconds = 0f
        renderedSecondsQueue.clear()
        isPlaying = false
        removeRequest = true
    }

    override fun pause() {
        if (audio.noDevice) return
        if (sourceId != -1) AL10.alSourcePause(sourceId)
        isPlaying = false
    }

    override fun setPan(pan: Float) {
        this.volume = volume
        this.pan = pan
        if (audio.noDevice) return
        if (sourceId == -1) return
        AL10.alSource3f(sourceId, AL10.AL_POSITION, cos((pan - 1) * PI.toFloat() / 2), 0f,
                sin((pan + 1) * PI.toFloat() / 2))
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume)
    }

    override var position: Float
        get() {
            if (audio.noDevice) return 0f
            return if (sourceId == -1) 0f else renderedSeconds + AL10.alGetSourcef(sourceId, AL11.AL_SEC_OFFSET)
        }
        set(value) {
            if (audio.noDevice) return
            if (sourceId == -1) return
            val wasPlaying = isPlaying
            isPlaying = false
            AL10.alSourceStop(sourceId)
            AL10.alSourceUnqueueBuffers(sourceId, buffers)
            while (renderedSecondsQueue.size > 0) {
                renderedSeconds = renderedSecondsQueue.pop()
            }
            if (value <= renderedSeconds) {
                reset()
                renderedSeconds = 0f
            }
            while (renderedSeconds < value - maxSecondsPerBuffer) {
                if (read(tempBytes) <= 0) break
                renderedSeconds += maxSecondsPerBuffer
            }
            renderedSecondsQueue.add(renderedSeconds)
            var filled = false
            for (i in 0 until bufferCount) {
                val bufferID = buffers!![i]
                if (!fill(bufferID)) break
                filled = true
                AL10.alSourceQueueBuffers(sourceId, bufferID)
            }
            renderedSecondsQueue.pop()
            if (!filled) {
                stop()
                if (onCompletionListener != null) onCompletionListener!!.onCompletion(this)
            }
            AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, value - renderedSeconds)
            if (wasPlaying) {
                AL10.alSourcePlay(sourceId)
                isPlaying = true
            }
        }

    /** Fills as much of the buffer as possible and returns the number of bytes filled. Returns <= 0 to indicate the end of the
     * stream.  */
    abstract fun read(buffer: ByteArray): Int

    /** Resets the stream to the beginning.  */
    abstract fun reset()

    /** By default, does just the same as reset(). Used to add special behaviour in Ogg.Music.  */
    protected open fun loop() {
        reset()
    }

    val channels: Int
        get() = if (format == AL10.AL_FORMAT_STEREO16) 2 else 1

    fun update() {
        if (audio.noDevice) return
        if (sourceId == -1) return
        var end = false
        var buffers = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED)
        while (buffers-- > 0) {
            val bufferID = AL10.alSourceUnqueueBuffers(sourceId)
            if (bufferID == AL10.AL_INVALID_VALUE) break
            if (renderedSecondsQueue.size > 0) renderedSeconds = renderedSecondsQueue.pop()
            if (end) continue
            if (fill(bufferID)) AL10.alSourceQueueBuffers(sourceId, bufferID) else end = true
        }
        if (end && AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED) == 0) {
            stop()
            if (onCompletionListener != null) onCompletionListener!!.onCompletion(this)
        }
        // A buffer underflow will cause the source to stop.
        if (isPlaying && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) AL10.alSourcePlay(sourceId)
    }

    private fun fill(bufferID: Int): Boolean {
        tempBuffer.clear()
        var length = read(tempBytes)
        if (length <= 0) {
            if (isLooping) {
                loop()
                length = read(tempBytes)
                if (length <= 0) return false
                if (renderedSecondsQueue.size > 0) {
                    renderedSecondsQueue[0] = 0f
                }
            } else return false
        }
        val previousLoadedSeconds: Float = if (renderedSecondsQueue.size > 0) renderedSecondsQueue.first() else 0f
        val currentBufferSeconds = maxSecondsPerBuffer * length.toFloat() / bufferSize.toFloat()
        renderedSecondsQueue.add(0, previousLoadedSeconds + currentBufferSeconds)
        tempBuffer.put(tempBytes, 0, length).flip()
        AL10.alBufferData(bufferID, format, tempBuffer, rate)
        return true
    }

    override fun destroy() {
        stop()
        if (audio.noDevice) return
        if (buffers == null) return
        AL10.alDeleteBuffers(buffers)
        buffers = null
        onCompletionListener = null
    }

    companion object {
        const val bufferSize = 4096 * 10
        const val bufferCount = 3
        const val bytesPerSample = 2
        private val tempBytes = ByteArray(bufferSize)
        private val tempBuffer = BufferUtils.createByteBuffer(bufferSize)
    }
}
