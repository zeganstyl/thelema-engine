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

import org.ksdfv.thelema.audio.IAudioRecorder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine


/** @author mzechner
 */
class JavaSoundAudioRecorder(samplingRate: Int, isMono: Boolean) : IAudioRecorder {
    private var line: TargetDataLine? = null
    private var buffer = ByteArray(1024 * 4)
    override fun read(samples: ShortArray, offset: Int, numSamples: Int) {
        if (buffer.size < numSamples * 2) buffer = ByteArray(numSamples * 2)
        val toRead = numSamples * 2
        var read = 0
        while (read != toRead) read += line!!.read(buffer, read, toRead - read)
        var i = 0
        var j = 0
        while (i < numSamples * 2) {
            samples[offset + j] = (buffer[i + 1].toInt() shl 8 or (buffer[i].toInt() and 0xff)).toShort()
            i += 2
            j++
        }
    }

    override fun dispose() {
        line!!.close()
    }

    init {
        try {
            val format = AudioFormat(Encoding.PCM_SIGNED, samplingRate.toFloat(), 16, if (isMono) 1 else 2, if (isMono) 2 else 4,
                    samplingRate.toFloat(), false)
            line = AudioSystem.getTargetDataLine(format)
            line!!.open(format, buffer.size)
            line!!.start()
        } catch (ex: Exception) {
            throw RuntimeException("Error creating JavaSoundAudioRecorder.", ex)
        }
    }
}
