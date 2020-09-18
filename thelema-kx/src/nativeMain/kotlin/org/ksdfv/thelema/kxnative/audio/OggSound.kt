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

package org.ksdfv.thelema.kxnative.audio

import kotlinx.cinterop.*
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.kxnative.data.NativeByteData
import org.ksdfv.thelema.kxnative.ubytePtr
import org.ksdfv.thelema.net.NET

/** STB Vorbis implementation
 *
 * @author zeganstyl */
class OggSound(audio: OpenAL, file: IFile) : OpenALSound(audio) {
    init {
        if (!audio.noDevice) {
            file.readByteData { status, data ->
                if (status == NET.OK) {
                    memScoped {
                        val channelsBuffer = alloc<IntVar>()
                        val sampleRateBuffer = alloc<IntVar>()

                        val rawAudioBuffer = allocPointerTo<ShortVar>()

                        val samplesPerChannel = glfw.stb_vorbis_decode_memory(
                            data.ubytePtr(),
                            data.size,
                            channelsBuffer.ptr,
                            sampleRateBuffer.ptr,
                            rawAudioBuffer.ptr
                        )

                        setup(
                            NativeByteData(samplesPerChannel * channelsBuffer.value * 2, rawAudioBuffer.value!!.reinterpret()),
                            channelsBuffer.value,
                            sampleRateBuffer.value
                        )
                    }
                } else {
                    throw IllegalArgumentException("Error reading file, status: $status")
                }
            }
        }
    }
}
