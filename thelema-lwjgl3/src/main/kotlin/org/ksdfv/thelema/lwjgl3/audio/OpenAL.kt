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

import org.ksdfv.thelema.audio.IAL
import org.ksdfv.thelema.audio.IAudioDevice
import org.ksdfv.thelema.audio.IAudioRecorder
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.math.MATH
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer


/** @author Nathan Sweet
 */
class OpenAL (simultaneousSources: Int = 16, private val deviceBufferCount: Int = 9, private val deviceBufferSize: Int = 512) : IAL {
    private val idleSources = ArrayList<Int>()
    private val allSources = ArrayList<Int>()
    private val soundIdToSource = HashMap<Long, Int>()
    private val sourceToSoundId = HashMap<Int, Long>()
    private var nextSoundId: Long = 0
    private val recentSounds = ArrayList<OpenALSound?>()
    private var mostRecentSound = -1
    var music: ArrayList<OpenALMusic> = ArrayList()
    val device: Long = ALC10.alcOpenDevice(null as ByteBuffer?)
    var context: Long
    var noDevice = false
    var checkAndValidatePlayingMusic = false

    override fun newSound(file: IFile): OpenALSound {
        val builder = OpenALCodecs.sound[file.extension.toLowerCase()] ?:
        throw IllegalArgumentException("Sound file extension is unknown")
        return builder.invoke(this, file)
    }

    override fun newMusic(file: IFile): OpenALMusic {
        val builder = OpenALCodecs.music[file.extension.toLowerCase()] ?:
        throw IllegalArgumentException("Music file extension is unknown")
        return builder.invoke(this, file)
    }

    fun obtainSource(isMusic: Boolean): Int {
        if (noDevice) return 0
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceId = idleSources[i]
            val state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
            if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
                if (isMusic) {
                    idleSources.remove(i)
                } else {
                    if (sourceToSoundId.contains(sourceId)) {
                        val soundId = sourceToSoundId[sourceId]
                        sourceToSoundId.remove(sourceId)
                        soundIdToSource.remove(soundId)
                    }
                    val soundId = nextSoundId++
                    sourceToSoundId[sourceId] = soundId
                    soundIdToSource[soundId] = sourceId
                }
                AL10.alSourceStop(sourceId)
                AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0)
                AL10.alSourcef(sourceId, AL10.AL_GAIN, 1f)
                AL10.alSourcef(sourceId, AL10.AL_PITCH, 1f)
                AL10.alSource3f(sourceId, AL10.AL_POSITION, 0f, 0f, 1f)
                return sourceId
            }
            i++
        }
        return -1
    }

    fun freeSource(sourceID: Int) {
        if (noDevice) return
        AL10.alSourceStop(sourceID)
        AL10.alSourcei(sourceID, AL10.AL_BUFFER, 0)
        if (sourceToSoundId.containsKey(sourceID)) {
            val soundId = sourceToSoundId.remove(sourceID)
            soundIdToSource.remove(soundId)
        }
        idleSources.add(sourceID)
    }

    fun freeBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                if (sourceToSoundId.containsKey(sourceID)) {
                    val soundId = sourceToSoundId.remove(sourceID)
                    soundIdToSource.remove(soundId)
                }
                AL10.alSourceStop(sourceID)
                AL10.alSourcei(sourceID, AL10.AL_BUFFER, 0)
            }
            i++
        }
    }

    fun stopSourcesWithBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                if (sourceToSoundId.containsKey(sourceID)) {
                    val soundId = sourceToSoundId.remove(sourceID)
                    soundIdToSource.remove(soundId)
                }
                AL10.alSourceStop(sourceID)
            }
            i++
        }
    }

    fun pauseSourcesWithBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) AL10.alSourcePause(sourceID)
            i++
        }
    }

    fun resumeSourcesWithBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (AL10.alGetSourcei(sourceID, AL10.AL_BUFFER) == bufferID) {
                if (AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) AL10.alSourcePlay(sourceID)
            }
            i++
        }
    }

    fun update() {
        if (noDevice) return
        if (checkAndValidatePlayingMusic) {
            music.removeIf {
                var b = false
                if (it.removeRequest) {
                    b = true
                    it.removeRequest = false
                }
                b
            }
            checkAndValidatePlayingMusic = false
        }
        for (i in 0 until music.size) music[i].update()
    }

    fun getSoundId(sourceId: Int): Long {
        return if (!sourceToSoundId.containsKey(sourceId)) -1 else sourceToSoundId[sourceId]!!
    }

    fun stopSound(soundId: Long) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]!!
        AL10.alSourceStop(sourceId)
    }

    fun pauseSound(soundId: Long) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]!!
        AL10.alSourcePause(sourceId)
    }

    fun resumeSound(soundId: Long) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        if (AL10.alGetSourcei(sourceId!!, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) AL10.alSourcePlay(sourceId)
    }

    fun setSoundGain(soundId: Long, volume: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        AL10.alSourcef(sourceId!!, AL10.AL_GAIN, volume)
    }

    fun setSoundLooping(soundId: Long, looping: Boolean) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        AL10.alSourcei(sourceId!!, AL10.AL_LOOPING, if (looping) AL10.AL_TRUE else AL10.AL_FALSE)
    }

    fun setSoundPitch(soundId: Long, pitch: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        AL10.alSourcef(sourceId!!, AL10.AL_PITCH, pitch)
    }

    fun setSoundPan(soundId: Long, pan: Float, volume: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        AL10.alSource3f(sourceId!!, AL10.AL_POSITION, MATH.cos((pan - 1) * MATH.PI / 2), 0f,
                MATH.sin((pan + 1) * MATH.PI / 2))
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume)
    }

    override fun destroy() {
        if (noDevice) return
        var i = 0
        val n = allSources.size
        while (i < n) {
            val sourceID = allSources[i]
            val state = AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE)
            if (state != AL10.AL_STOPPED) AL10.alSourceStop(sourceID)
            AL10.alDeleteSources(sourceID)
            i++
        }
        sourceToSoundId.clear()
        soundIdToSource.clear()
        ALC10.alcDestroyContext(context)
        ALC10.alcCloseDevice(device)
    }

    override fun newAudioDevice(samplingRate: Int, isMono: Boolean): IAudioDevice {
        return if (noDevice) object : IAudioDevice {
            override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {}
            override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun setVolume(volume: Float) {}

            override val isMono: Boolean
                get() = isMono

            override val latency: Int
                get() = 0

            override fun dispose() {}
        } else OpenALAudioDevice(this, samplingRate, isMono, deviceBufferSize, deviceBufferCount)
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        return if (noDevice) object : IAudioRecorder {
            override fun read(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun dispose() {}
        } else JavaSoundAudioRecorder(samplingRate, isMono)
    }

    override fun getVersion(param: Int): String = AL10.alGetString(param) ?: ""

    /** Retains a list of the most recently played sounds and stops the sound played least recently if necessary for a new sound to
     * play  */
    fun retain(sound: OpenALSound?, stop: Boolean) { // Move the pointer ahead and wrap
        mostRecentSound++
        mostRecentSound %= recentSounds.size
        if (stop) { // Stop the least recent sound (the one we are about to bump off the buffer)
            if (recentSounds[mostRecentSound] != null) recentSounds[mostRecentSound]!!.stop()
        }

        recentSounds[mostRecentSound] = sound
    }

    /** Removes the disposed sound from the least recently played list  */
    fun forget(sound: OpenALSound) {
        for (i in 0 until recentSounds.size) {
            if (recentSounds[i] === sound) recentSounds[i] = null
        }
    }

    init {
        if (device == 0L) {
            noDevice = true
        }
        val deviceCapabilities = ALC.createCapabilities(device)
        context = ALC10.alcCreateContext(device, null as IntBuffer?)
        if (context == 0L) {
            ALC10.alcCloseDevice(device)
            noDevice = true
        }
        if (!ALC10.alcMakeContextCurrent(context)) {
            noDevice = true
        }
        AL.createCapabilities(deviceCapabilities)
        for (i in 0 until simultaneousSources) {
            val sourceID = AL10.alGenSources()
            if (AL10.alGetError() != AL10.AL_NO_ERROR) break
            allSources.add(sourceID)
            idleSources.add(sourceID)
        }
        val orientation = BufferUtils.createFloatBuffer(6)
                .put(floatArrayOf(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)).flip() as FloatBuffer
        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation)
        val velocity = BufferUtils.createFloatBuffer(3).put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer
        AL10.alListenerfv(AL10.AL_VELOCITY, velocity)
        val position = BufferUtils.createFloatBuffer(3).put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer
        AL10.alListenerfv(AL10.AL_POSITION, position)

        for (i in 0 until 16) {
            recentSounds.add(null)
        }
    }
}
