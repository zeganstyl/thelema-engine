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

import kotlinx.cinterop.*
import app.thelema.audio.IMusic
import app.thelema.fs.IFile
import app.thelema.ptr
import app.thelema.uint
import app.thelema.math.MATH

/** @author Nathan Sweet
 */
abstract class OpenALMusic(private val audio: OpenAL, protected val file: IFile) : IMusic {
    private val renderedSecondsQueue = ArrayList<Float>()
    private var buffers: CPointer<UIntVar>? = null
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
            if (sourceId != -1) al.alSourcef(sourceId.uint(), al.AL_GAIN, value)
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
        format = if (channels > 1) al.AL_FORMAT_STEREO16 else al.AL_FORMAT_MONO16
        rate = sampleRate
        maxSecondsPerBuffer = audio.bufferSize.toFloat() / (audio.bytesPerSample * channels * sampleRate)
    }

    override fun play() {
        if (audio.noDevice) return
        if (sourceId == -1) {
            sourceId = audio.obtainSource(true)
            if (sourceId == -1) return
            audio.music.add(this)
            if (buffers == null) {
                buffers = nativeHeap.allocArray(audio.bufferCount)
                al.alGenBuffers(audio.bufferCount, buffers)
                val errorCode = al.alGetError()
                if (errorCode != al.AL_NO_ERROR) throw RuntimeException("Unable to allocate audio buffers. AL Error: $errorCode")
            }
            //al.alSourcei(sourceId.uint(), SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, al.AL_TRUE)
            al.alSourcei(sourceId.uint(), al.AL_LOOPING, al.AL_FALSE)
            setPan(pan)
            var filled = false // Check if there's anything to actually play.
            for (i in 0 until audio.bufferCount) {
                val bufferID = buffers!![i]
                if (!fill(bufferID.toInt())) break
                filled = true
                audio.uintVar.value = bufferID
                al.alSourceQueueBuffers(sourceId.uint(), 1, audio.uintVar.ptr)
            }
            if (!filled && onCompletionListener != null) onCompletionListener!!.onCompletion(this)
            if (al.alGetError() != al.AL_NO_ERROR) {
                stop()
                return
            }
        }
        if (!isPlaying) {
            al.alSourcePlay(sourceId.uint())
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
        if (sourceId != -1) al.alSourcePause(sourceId.uint())
        isPlaying = false
    }

    override fun setPan(pan: Float) {
        this.volume = volume
        this.pan = pan
        if (audio.noDevice) return
        if (sourceId == -1) return
        al.alSource3f(
            sourceId.uint(),
            al.AL_POSITION,
            MATH.cos((pan - 1) * MATH.PI * 0.5f),
            0f,
            MATH.sin((pan + 1) * MATH.PI * 0.5f)
        )
        al.alSourcef(sourceId.uint(), al.AL_GAIN, volume)
    }

    override var position: Float
        get() {
            if (audio.noDevice) return 0f
            return if (sourceId == -1) {
                0f
            } else {
                al.alGetSourcef(sourceId.uint(), al.AL_SEC_OFFSET, audio.floatVar.ptr)
                renderedSeconds + audio.floatVar.value
            }
        }
        set(value) {
            if (audio.noDevice) return
            if (sourceId == -1) return
            val wasPlaying = isPlaying
            isPlaying = false
            al.alSourceStop(sourceId.uint())
            al.alSourceUnqueueBuffers(sourceId.uint(), audio.bufferCount, buffers)
            while (renderedSecondsQueue.size > 0) {
                renderedSeconds = renderedSecondsQueue.removeAt(renderedSecondsQueue.lastIndex)
            }
            if (value <= renderedSeconds) {
                reset()
                renderedSeconds = 0f
            }
            while (renderedSeconds < value - maxSecondsPerBuffer) {
                if (read(audio.tempBytes) <= 0) break
                renderedSeconds += maxSecondsPerBuffer
            }
            renderedSecondsQueue.add(renderedSeconds)
            var filled = false
            for (i in 0 until audio.bufferCount) {
                val bufferID = buffers!![i]
                if (!fill(bufferID.toInt())) break
                filled = true
                audio.uintVar.value = bufferID
                al.alSourceQueueBuffers(sourceId.uint(), 1, audio.uintVar.ptr)
            }
            renderedSecondsQueue.removeAt(renderedSecondsQueue.lastIndex)
            if (!filled) {
                stop()
                if (onCompletionListener != null) onCompletionListener!!.onCompletion(this)
            }
            al.alSourcef(sourceId.uint(), al.AL_SEC_OFFSET, value - renderedSeconds)
            if (wasPlaying) {
                al.alSourcePlay(sourceId.uint())
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
        get() = if (format == al.AL_FORMAT_STEREO16) 2 else 1

    fun update() {
        if (audio.noDevice) return
        if (sourceId == -1) return
        var end = false
        var buffers = audio.alGetSource(sourceId, al.AL_BUFFERS_PROCESSED)
        while (buffers-- > 0) {
            al.alSourceUnqueueBuffers(sourceId.uint(), 1, audio.uintVar.ptr)
            val bufferID = audio.uintVar.value.toInt()
            if (bufferID == al.AL_INVALID_VALUE) break
            if (renderedSecondsQueue.size > 0) renderedSeconds = renderedSecondsQueue.removeAt(renderedSecondsQueue.lastIndex)
            if (end) continue
            if (fill(bufferID)) {
                audio.uintVar.value = bufferID.toUInt()
                al.alSourceQueueBuffers(sourceId.uint(), 1, audio.uintVar.ptr)
            } else {
                end = true
            }
        }
        if (end && audio.alGetSource(sourceId, al.AL_BUFFERS_QUEUED) == 0) {
            stop()
            if (onCompletionListener != null) onCompletionListener!!.onCompletion(this)
        }
        // A buffer underflow will cause the source to stop.
        if (isPlaying && audio.alGetSource(sourceId, al.AL_SOURCE_STATE) != al.AL_PLAYING) al.alSourcePlay(sourceId.uint())
    }

    private fun fill(bufferID: Int): Boolean {
        audio.tempBuffer.clear()
        var length = read(audio.tempBytes)
        if (length <= 0) {
            if (isLooping) {
                loop()
                length = read(audio.tempBytes)
                if (length <= 0) return false
                if (renderedSecondsQueue.size > 0) {
                    renderedSecondsQueue[0] = 0f
                }
            } else return false
        }
        val previousLoadedSeconds: Float = if (renderedSecondsQueue.size > 0) renderedSecondsQueue.first() else 0f
        val currentBufferSeconds = maxSecondsPerBuffer * length.toFloat() / audio.bufferSize.toFloat()
        renderedSecondsQueue.add(0, previousLoadedSeconds + currentBufferSeconds)
        audio.tempBuffer.put(audio.tempBytes, 0, length)
        audio.tempBuffer.flip()
        al.alBufferData(bufferID.uint(), format, audio.tempBuffer.ptr(), audio.tempBuffer.limit, rate)
        return true
    }

    override fun destroy() {
        stop()
        if (audio.noDevice) return
        if (buffers == null) return
        al.alDeleteBuffers(audio.bufferCount, buffers)
        nativeHeap.free(buffers!!)
        buffers = null
        onCompletionListener = null
    }
}
