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
import app.thelema.data.DATA
import app.thelema.fs.IFile
import app.thelema.uint
import app.thelema.math.MATH


/** @author Nathan Sweet
 */
class OpenAL (simultaneousSources: Int = 16, private val deviceBufferCount: Int = 9, private val deviceBufferSize: Int = 512) : IAudio {
    private val idleSources = ArrayList<Int>()
    private val allSources = ArrayList<Int>()
    private val soundIdToSource = HashMap<Int, Int>()
    private val sourceToSoundId = HashMap<Int, Int>()
    private var nextSoundId: Int = 0
    private val recentSounds = ArrayList<OpenALSound?>()
    private var mostRecentSound = -1
    val music: ArrayList<OpenALMusic> = ArrayList()
    val device: CPointer<cnames.structs.ALCdevice>? = al.alcOpenDevice(null)
    var context: CPointer<cnames.structs.ALCcontext>?
    var noDevice = false
    var checkAndValidatePlayingMusic = false

    val bufferSize = 4096 * 10
    val bufferCount = 3
    val bytesPerSample = 2

    val tempBytes = ByteArray(bufferSize)
    val tempBuffer = DATA.bytes(bufferSize)
    val shortBuffer = tempBuffer.shortView()

    val tmpMusic = ArrayList<OpenALMusic>()

    /** Key - file extension in lowercase, value - sound object builder */
    val soundCodecs = HashMap<String, (audio: OpenAL, file: IFile) -> OpenALSound>()

    /** Key - file extension in lowercase, value - music object builder */
    val musicCodecs = HashMap<String, (audio: OpenAL, file: IFile) -> OpenALMusic>()

    val uintVar = nativeHeap.alloc<UIntVar>()
    val intVar = nativeHeap.alloc<IntVar>()
    val floatVar = nativeHeap.alloc<FloatVar>()

    init {
        soundCodecs["wav"] = { audio, file -> WavSoundLoader(audio, file) }
//        musicCodecs["wav"] = { audio, file -> WavMusic(audio, file) }
//
        soundCodecs["ogg"] = { audio, file -> OggSoundLoader(audio, file) }
//        musicCodecs["ogg"] = { audio, file -> OggMusic(audio, file) }
    }

    override fun newSound(file: IFile): OpenALSound {
        val builder = soundCodecs[file.extension.toLowerCase()] ?:
        throw IllegalArgumentException("Sound file extension is unknown")
        return builder.invoke(this, file)
    }

    override fun newMusic(file: IFile): OpenALMusic {
        val builder = musicCodecs[file.extension.toLowerCase()] ?:
        throw IllegalArgumentException("Music file extension is unknown")
        return builder.invoke(this, file)
    }

    fun obtainSource(isMusic: Boolean): Int {
        if (noDevice) return 0
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceId = idleSources[i]
            val source = sourceId.uint()
            memScoped {
                val state = alloc<IntVar>()
                al.alGetSourcei(source, al.AL_SOURCE_STATE, state.ptr)
                if (state.value != al.AL_PLAYING && state.value != al.AL_PAUSED) {
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
                    al.alSourceStop(source)
                    al.alSourcei(source, al.AL_BUFFER, 0)
                    al.alSourcef(source, al.AL_GAIN, 1f)
                    al.alSourcef(source, al.AL_PITCH, 1f)
                    al.alSource3f(source, al.AL_POSITION, 0f, 0f, 1f)
                    return sourceId
                }
            }
            i++
        }
        return -1
    }

    fun freeSource(sourceID: Int) {
        if (noDevice) return
        al.alSourceStop(sourceID.uint())
        al.alSourcei(sourceID.uint(), al.AL_BUFFER, 0)
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
            val source = sourceID.uint()
            memScoped {
                val b = alloc<IntVar>()
                al.alGetSourcei(source, al.AL_BUFFER, b.ptr)
                if (b.value == bufferID) {
                    if (sourceToSoundId.containsKey(sourceID)) {
                        val soundId = sourceToSoundId.remove(sourceID)
                        soundIdToSource.remove(soundId)
                    }
                    al.alSourceStop(source)
                    al.alSourcei(source, al.AL_BUFFER, 0)
                }
            }
            i++
        }
    }

    fun alGetSource(source: Int, param: Int): Int {
        al.alGetSourcei(source.uint(), param, intVar.ptr)
        return intVar.value
    }

    fun stopSourcesWithBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (alGetSource(sourceID, al.AL_BUFFER) == bufferID) {
                if (sourceToSoundId.containsKey(sourceID)) {
                    val soundId = sourceToSoundId.remove(sourceID)
                    soundIdToSource.remove(soundId)
                }
                al.alSourceStop(sourceID.uint())
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
            if (alGetSource(sourceID, al.AL_BUFFER) == bufferID) al.alSourcePause(sourceID.uint())
            i++
        }
    }

    fun resumeSourcesWithBuffer(bufferID: Int) {
        if (noDevice) return
        var i = 0
        val n = idleSources.size
        while (i < n) {
            val sourceID = idleSources[i]
            if (alGetSource(sourceID, al.AL_BUFFER) == bufferID) {
                if (alGetSource(sourceID, al.AL_SOURCE_STATE) == al.AL_PAUSED) al.alSourcePlay(sourceID.uint())
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
    }

    fun getSoundId(sourceId: Int): Int {
        return if (!sourceToSoundId.containsKey(sourceId)) -1 else sourceToSoundId[sourceId]!!
    }

    fun stopSound(soundId: Int) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]!!
        al.alSourceStop(sourceId.uint())
    }

    fun pauseSound(soundId: Int) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]!!
        al.alSourcePause(sourceId.uint())
    }

    fun resumeSound(soundId: Int) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        if (alGetSource(sourceId!!, al.AL_SOURCE_STATE) == al.AL_PAUSED) al.alSourcePlay(sourceId.uint())
    }

    fun setSoundGain(soundId: Int, volume: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        al.alSourcef(sourceId!!.uint(), al.AL_GAIN, volume)
    }

    fun setSoundLooping(soundId: Int, looping: Boolean) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        al.alSourcei(sourceId!!.uint(), al.AL_LOOPING, if (looping) al.AL_TRUE else al.AL_FALSE)
    }

    fun setSoundPitch(soundId: Int, pitch: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        al.alSourcef(sourceId!!.uint(), al.AL_PITCH, pitch)
    }

    fun setSoundPan(soundId: Int, pan: Float) {
        if (!soundIdToSource.containsKey(soundId)) return
        val sourceId = soundIdToSource[soundId]
        al.alSource3f(sourceId!!.uint(), al.AL_POSITION, MATH.cos((pan - 1) * MATH.PI / 2), 0f,
                MATH.sin((pan + 1) * MATH.PI / 2))
    }

    override fun destroy() {
        nativeHeap.free(uintVar)
        if (noDevice) return
        memScoped {
            val n = allSources.size
            val sources = allocArray<UIntVar>(n)
            var i = 0
            while (i < n) {
                val sourceID = allSources[i]
                val state = alGetSource(sourceID, al.AL_SOURCE_STATE)
                if (state != al.AL_STOPPED) al.alSourceStop(sourceID.uint())
                sources[i] = sourceID.toUInt()
                i++
            }
            al.alDeleteSources(n, sources)
        }
        sourceToSoundId.clear()
        soundIdToSource.clear()
        al.alcDestroyContext(context)
        al.alcCloseDevice(device)
    }

    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice {
        return object : IAudioDevice {
            override val channelsNum: Int
                get() = channelsNum

            override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {}
            override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun setVolume(volume: Float) {}

            override fun destroy() {}
        }
//        return if (noDevice) object : IAudioDevice {
//            override val channelsNum: Int
//                get() = channelsNum
//
//            override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {}
//            override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {}
//            override fun setVolume(volume: Float) {}
//
//            override fun destroy() {}
//        } else OpenALAudioDevice(this, samplingRate, channelsNum, deviceBufferSize, deviceBufferCount)
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        return object : IAudioRecorder {
            override fun read(samples: ShortArray, offset: Int, numSamples: Int) {}
            override fun dispose() {}
        }
    }

    override fun getVersion(param: Int): String = al.alGetString(param)?.toKString() ?: ""

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
        if (device == null) {
            noDevice = true
        }
        context = al.alcCreateContext(device, null)
        if (context == null) {
            al.alcCloseDevice(device)
            noDevice = true
        }
        if (al.alcMakeContextCurrent(context).toInt() != 1) {
            noDevice = true
        }
        memScoped {
            val sources = allocArray<UIntVar>(simultaneousSources)
            al.alGenSources(simultaneousSources, sources)
            for (i in 0 until simultaneousSources) {
                if (al.alGetError() != al.AL_NO_ERROR) break
                val id = sources[i].toInt()
                allSources.add(id)
                idleSources.add(id)
            }
        }
        al.alListenerfv(al.AL_ORIENTATION, cValuesOf(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f))
        al.alListenerfv(al.AL_VELOCITY, cValuesOf(0.0f, 0.0f, 0.0f))
        al.alListenerfv(al.AL_POSITION, cValuesOf(0.0f, 0.0f, 0.0f))

        for (i in 0 until 16) {
            recentSounds.add(null)
        }
    }
}
