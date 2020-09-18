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

package org.ksdfv.thelema.kxjs.audio

import org.khronos.webgl.set
import org.ksdfv.thelema.audio.IAudioDevice

/** @author zeganstyl */
class JsAudioDevice(
    val al: JsAL,
    val sampleRate: Int,
    override val channelsNum: Int
): IAudioDevice {
    var buffer: AudioBuffer = al.context.createBuffer(channelsNum, 0, sampleRate)
    val source = al.context.createBufferSource()
    val gain = al.context.createGain()

    init {
        source.connect(gain)
        gain.connect(al.context.destination)
    }

    override fun writeSamples(samples: ShortArray, offset: Int, numSamples: Int) {
        if (buffer.length < numSamples) {
            buffer = al.context.createBuffer(channelsNum, numSamples, sampleRate)
        }
        val buffer = al.context.createBuffer(channelsNum, numSamples, sampleRate)
        for (i in 0 until buffer.numberOfChannels) {
            val channel = buffer.getChannelData(i)
            for (j in 0 until numSamples) {
                channel[j] = samples[i].toFloat()
            }
        }

        source.buffer = buffer
        source.start(0.0, 0.0, numSamples.toDouble() / sampleRate)
    }

    override fun writeSamples(samples: FloatArray, offset: Int, numSamples: Int) {
        if (buffer.length < numSamples) {
            buffer = al.context.createBuffer(channelsNum, numSamples, sampleRate)
        }
        val buffer = al.context.createBuffer(channelsNum, numSamples, sampleRate)
        for (i in 0 until buffer.numberOfChannels) {
            val channel = buffer.getChannelData(i)
            for (j in 0 until numSamples) {
                channel[j] = samples[i]
            }
        }

        source.buffer = buffer
        source.start(0.0, 0.0, numSamples.toDouble() / sampleRate)
    }

    override fun destroy() {
        source.disconnect()
        buffer = al.context.createBuffer(channelsNum, 0, sampleRate)
    }

    override fun setVolume(volume: Float) {
        gain.gain.value = volume
    }
}