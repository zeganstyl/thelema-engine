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

import org.ksdfv.thelema.fs.IFile
import java.io.IOException

class WavMusic(audio: OpenAL, file: IFile) : OpenALMusic(audio, file) {
    private var input: WavInputStream? = WavInputStream(file)
    override fun read(buffer: ByteArray): Int {
        if (input == null) {
            input = WavInputStream(file)
            setup(input!!.channels, input!!.sampleRate)
        }
        return try {
            input!!.read(buffer)
        } catch (ex: IOException) {
            throw RuntimeException("Error reading WAV file: $file", ex)
        }
    }

    override fun reset() {
        input?.close()
        input = null
    }

    init {
        if (!audio.noDevice) {
            setup(input!!.channels, input!!.sampleRate)
        }
    }
}