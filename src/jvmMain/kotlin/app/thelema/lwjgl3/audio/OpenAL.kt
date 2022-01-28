/*
 * Copyright 2020-2021 Anton Trushkov
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

package app.thelema.lwjgl3.audio

import app.thelema.audio.IAudio
import app.thelema.audio.IAudioDevice
import app.thelema.audio.IAudioRecorder
import app.thelema.audio.audioListener
import app.thelema.fs.IFile
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.math.Vec3
import app.thelema.res.RES
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer


/** @author Nathan Sweet, zeganstyl */
class OpenAL (simultaneousSources: Int = 16, private val deviceBufferCount: Int = 9, private val deviceBufferSize: Int = 512) : IAudio {
    private val idleSources = ArrayList<Int>()
    private val allSources = ArrayList<Int>()
    private val soundIdToSource = HashMap<Int, Int>()
    private val sourceToSoundId = HashMap<Int, Int>()
    private var nextSoundId: Int = 0
    private val recentSounds = ArrayList<SoundLoader?>()
    val music: ArrayList<OpenALMusic> = ArrayList()
    val device: Long = ALC10.alcOpenDevice(null as ByteBuffer?)
    var context: Long
    var noDevice = false
    var checkAndValidatePlayingMusic = false

    val tmpMusic = ArrayList<OpenALMusic>()

    override fun newMusic(file: IFile): OpenALMusic {
        val builder = OpenALCodecs.music[file.extension.lowercase()] ?:
        throw IllegalArgumentException("Music file extension is unknown")
        return builder.invoke(this, file)
    }

    fun obtainSource(isMusic: Boolean): Int {
        if (noDevice) return 0
//        var i = 0
//        val n = idleSources.size
//        while (i < n) {
//            val sourceId = idleSources[i]
//            val state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
//            if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
//                if (isMusic) {
//                    idleSources.remove(i)
//                } else {
//                    if (sourceToSoundId.contains(sourceId)) {
//                        val soundId = sourceToSoundId[sourceId]
//                        sourceToSoundId.remove(sourceId)
//                        soundIdToSource.remove(soundId)
//                    }
//                    val soundId = nextSoundId++
//                    sourceToSoundId[sourceId] = soundId
//                    soundIdToSource[soundId] = sourceId
//                }
//                AL10.alSourceStop(sourceId)
//                AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0)
//                AL10.alSourcef(sourceId, AL10.AL_GAIN, 1f)
//                AL10.alSourcef(sourceId, AL10.AL_PITCH, 1f)
//                AL10.alSource3f(sourceId, AL10.AL_POSITION, 0f, 0f, 1f)
//                return sourceId
//            }
//            i++
//        }
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

    override fun update() {
        if (noDevice) return
        if (checkAndValidatePlayingMusic) {
            tmpMusic.clear()
            val music = music
            for (i in music.indices) {
                val mus = music[i]
                if (mus.removeRequest) {
                    tmpMusic.add(mus)
                    mus.removeRequest = false
                }
            }
            for (i in tmpMusic.indices) {
                music.remove(tmpMusic[i])
            }
            tmpMusic.clear()
            checkAndValidatePlayingMusic = false
        }
        for (i in 0 until music.size) music[i].update()
        RES.getOrCreateEntity().audioListener().updateListener(0f)
    }

    val tmp = Vec3()
    private val position = BufferUtils.createFloatBuffer(3)
        .put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer

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

    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice {
        return if (noDevice) object : IAudioDevice {
            override val channelsNum: Int
                get() = channelsNum

            override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {}
            override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun setVolume(volume: Float) {}

            override fun destroy() {}
        } else OpenALAudioDevice(this, samplingRate, channelsNum, deviceBufferSize, deviceBufferCount)
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        return if (noDevice) object : IAudioRecorder {
            override fun read(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun dispose() {}
        } else JavaSoundAudioRecorder(samplingRate, isMono)
    }

    override fun getVersion(param: Int): String = AL10.alGetString(param) ?: ""

    /** Removes the disposed sound from the least recently played list  */
    fun forget(sound: SoundLoader) {
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
//        for (i in 0 until simultaneousSources) {
//            val sourceID = AL10.alGenSources()
//            if (AL10.alGetError() != AL10.AL_NO_ERROR) break
//            allSources.add(sourceID)
//            idleSources.add(sourceID)
//        }
    }
}
