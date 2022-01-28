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

import app.thelema.audio.AL
import app.thelema.audio.ISoundLoader
import app.thelema.fs.IFile
import app.thelema.math.MATH
import app.thelema.res.LoaderAdapter
import app.thelema.utils.LOG
import org.lwjgl.openal.AL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/** @author Nathan Sweet, zeganstyl */
class SoundLoader : ISoundLoader, LoaderAdapter() {
    var bufferID = -1
    override var duration: Float = 0f

    private val audio: OpenAL
        get() = AL as OpenAL

    override val componentName: String
        get() = "SoundLoader"

    var useMono: Boolean = true

    override fun loadBase(file: IFile) {
        val codec = OpenALCodecs.sound[file.extension.lowercase()]
        if (codec == null) {
            LOG.error("Sound file extension is unknown: ${file.path}")
            stop(0)
            return
        }
        codec.invoke(file, this)
        stop(200)
    }

    fun setup(pcm: ByteBuffer, channels: Int, sampleRate: Int) {
        val bytesNum = pcm.limit() - pcm.limit() % if (channels > 1) 4 else 2
        val samples = bytesNum / (2 * channels)
        duration = samples / sampleRate.toFloat()
        if (bufferID == -1) {
            bufferID = AL10.alGenBuffers()
            if (useMono) {
                if (channels > 1) {
                    val monoBuffer = ByteBuffer.allocateDirect(samples * 2)
                    val l = pcm.limit()
                    var i = 0
                    val step = channels * 2
                    while (i < l) {
                        monoBuffer.put(pcm.get(i))
                        monoBuffer.put(pcm.get(i + 1))
                        i += step
                    }
                    monoBuffer.rewind()
                    AL10.alBufferData(bufferID, AL10.AL_FORMAT_MONO16, monoBuffer, sampleRate)
                } else {
                    AL10.alBufferData(bufferID, AL10.AL_FORMAT_MONO16, pcm, sampleRate)
                }
            } else {
                AL10.alBufferData(bufferID, if (channels > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16, pcm, sampleRate)
            }
        }
    }

    fun setup(pcm: ShortBuffer, channels: Int, sampleRate: Int) {
        val bytesNum = (pcm.limit() - pcm.limit() % channels) * 2
        val samples = bytesNum / (2 * channels)
        duration = samples / sampleRate.toFloat()
        if (bufferID == -1) {
            bufferID = AL10.alGenBuffers()
            AL10.alBufferData(bufferID, if (channels > 1) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16, pcm, sampleRate)
        }
    }

    override fun play(volume: Float, pitch: Float, pan: Float, loop: Boolean): Int {
//        if (audio.noDevice) return 0
//        var sourceID = audio.obtainSource(false)
//        if (sourceID == -1) {
//            // Attempt to recover by stopping the least recently played sound
//            audio.retain(this, true)
//            sourceID = audio.obtainSource(false)
//        } else {
//            audio.retain(this, false)
//        }
//
//        // In case it still didn't work
//        if (sourceID == -1) return -1
//
//        LOG.error("play $sourceID")
//
//        val soundId = audio.getSoundId(sourceID)
//        AL10.alSourcei(sourceID, AL10.AL_BUFFER, bufferID)
//        AL10.alSourcei(sourceID, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)
//        AL10.alSourcef(sourceID, AL10.AL_GAIN, volume)
//        AL10.alSourcef(sourceID, AL10.AL_PITCH, pitch)
//        AL10.alSource3f(sourceID, AL10.AL_POSITION, MATH.cos((pan - 1) * MATH.PI / 2), 0f,
//            MATH.sin((pan + 1) * MATH.PI / 2))
//        AL10.alSourcePlay(sourceID)
//        return soundId
        return 0
    }

    override fun stopSound() {
        //audio.stopSourcesWithBuffer(bufferID)
    }

    override fun destroy() {
        if (bufferID == -1) return
        audio.freeBuffer(bufferID)
        AL10.alDeleteBuffers(bufferID)
        bufferID = -1
        audio.forget(this)
    }

    override fun stopSound(soundId: Int) {
        //audio.stopSound(soundId)
    }

    override fun pause() {
        //audio.pauseSourcesWithBuffer(bufferID)
    }

    override fun pause(soundId: Int) {
        //audio.pauseSound(soundId)
    }

    override fun resume() {
        //audio.resumeSourcesWithBuffer(bufferID)
    }

    override fun resume(soundId: Int) {
        //audio.resumeSound(soundId)
    }

    override fun setPitch(soundId: Int, pitch: Float) {
        //audio.setSoundPitch(soundId, pitch)
    }

    override fun setVolume(soundId: Int, volume: Float) {
        //audio.setSoundGain(soundId, volume)
    }

    override fun setLooping(soundId: Int, looping: Boolean) {
        //audio.setSoundLooping(soundId, looping)
    }

    override fun setPan(soundId: Int, pan: Float) {
        //audio.setSoundPan(soundId, pan)
    }
}
