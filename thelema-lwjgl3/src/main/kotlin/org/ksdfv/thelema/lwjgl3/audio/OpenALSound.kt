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

import org.ksdfv.thelema.audio.ISound
import org.lwjgl.openal.AL10
import java.nio.ByteBuffer
import java.nio.ByteOrder


/** @author Nathan Sweet
 */
abstract class OpenALSound(private val audio: OpenAL) : ISound {
    private var bufferID = -1
    var duration = 0f
        private set

    fun setup(pcm: ByteArray, channels: Int, sampleRate: Int) {
        val bytes = pcm.size - pcm.size % if (channels > 1) 4 else 2
        val samples = bytes / (2 * channels)
        duration = samples / sampleRate.toFloat()
        val buffer = ByteBuffer.allocateDirect(bytes)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(pcm, 0, bytes)
        buffer.flip()
        if (bufferID == -1) {
            bufferID = AL10.alGenBuffers()
            AL10.alBufferData(bufferID, if (channels > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16, buffer.asShortBuffer(), sampleRate)
        }
    }

    override fun play(volume: Float, pitch: Float, pan: Float, loop: Boolean): Int {
        if (audio.noDevice) return 0
        var sourceID = audio.obtainSource(false)
        if (sourceID == -1) { // Attempt to recover by stopping the least recently played sound
            audio.retain(this, true)
            sourceID = audio.obtainSource(false)
        } else audio.retain(this, false)
        // In case it still didn't work
        if (sourceID == -1) return -1
        val soundId = audio.getSoundId(sourceID)
        AL10.alSourcei(sourceID, AL10.AL_BUFFER, bufferID)
        AL10.alSourcei(sourceID, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)
        AL10.alSourcef(sourceID, AL10.AL_GAIN, volume)
        setPitch(soundId, pitch)
        setPan(soundId, pan)
        AL10.alSourcePlay(sourceID)
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
        AL10.alDeleteBuffers(bufferID)
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
