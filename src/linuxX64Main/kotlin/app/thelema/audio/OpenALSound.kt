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
import app.thelema.audio.ISound
import app.thelema.data.IByteData
import app.thelema.ptr
import app.thelema.uint

/** @author Nathan Sweet */
open class OpenALSound(private val audio: OpenAL) : ISound {
    private var bufferID = -1
    var duration = 0f
        private set

    fun setup(pcm: IByteData, channels: Int, sampleRate: Int) {
        val samples = pcm.limit / (2 * channels)
        duration = samples / sampleRate.toFloat()
        if (bufferID == -1) {
            memScoped {
                val b = alloc<UIntVar>()
                al.alGenBuffers(1, b.ptr)
                bufferID = b.value.toInt()
                al.alBufferData(
                    b.value,
                    if (channels > 1) al.AL_FORMAT_STEREO16 else al.AL_FORMAT_MONO16,
                    pcm.ptr(),
                    pcm.limit,
                    sampleRate
                )
            }
        }
    }

    override fun play(volume: Float, pitch: Float, pan: Float, loop: Boolean): Int {
        if (audio.noDevice) return 0
        var sourceID = audio.obtainSource(false)
        if (sourceID == -1) {
            // Attempt to recover by stopping the least recently played sound
            audio.retain(this, true)
            sourceID = audio.obtainSource(false)
        } else {
            audio.retain(this, false)
        }

        // In case it still didn't work
        if (sourceID == -1) return -1

        val soundId = audio.getSoundId(sourceID)
        val source = sourceID.uint()
        al.alSourcei(source, al.AL_BUFFER, bufferID)
        al.alSourcei(source, al.AL_LOOPING, if (loop) al.AL_TRUE else al.AL_FALSE)
        al.alSourcef(source, al.AL_GAIN, volume)
        setPitch(soundId, pitch)
        setPan(soundId, pan)
        al.alSourcePlay(source)
        return soundId
    }

    override fun stop() {
        if (audio.noDevice) return
        audio.stopSourcesWithBuffer(bufferID)
    }

    override fun destroy() {
        if (audio.noDevice) return
        if (bufferID == -1) return
        audio.freeBuffer(bufferID)
        memScoped {
            val b = alloc<UIntVar>()
            b.value = bufferID.uint()
            al.alDeleteBuffers(1, b.ptr)
        }
        bufferID = -1
        audio.forget(this)
    }

    override fun stop(soundId: Int) {
        if (audio.noDevice) return
        audio.stopSound(soundId)
    }

    override fun pause() {
        if (audio.noDevice) return
        audio.pauseSourcesWithBuffer(bufferID)
    }

    override fun pause(soundId: Int) {
        if (audio.noDevice) return
        audio.pauseSound(soundId)
    }

    override fun resume() {
        if (audio.noDevice) return
        audio.resumeSourcesWithBuffer(bufferID)
    }

    override fun resume(soundId: Int) {
        if (audio.noDevice) return
        audio.resumeSound(soundId)
    }

    override fun setPitch(soundId: Int, pitch: Float) {
        if (audio.noDevice) return
        audio.setSoundPitch(soundId, pitch)
    }

    override fun setVolume(soundId: Int, volume: Float) {
        if (audio.noDevice) return
        audio.setSoundGain(soundId, volume)
    }

    override fun setLooping(soundId: Int, looping: Boolean) {
        if (audio.noDevice) return
        audio.setSoundLooping(soundId, looping)
    }

    override fun setPan(soundId: Int, pan: Float) {
        if (audio.noDevice) return
        audio.setSoundPan(soundId, pan)
    }
}
