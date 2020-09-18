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

package org.ksdfv.thelema.test.audio

import org.ksdfv.thelema.audio.AL
import org.ksdfv.thelema.test.Test
import kotlin.random.Random

/** @author zeganstyl */
class AudioDeviceTest: Test {
    override val name: String
        get() = "Audio Device"

    override fun testMain() {
        // 500 samples in second, 5 seconds of noise
        val sampleRate = 500
        val samples = ShortArray(sampleRate * 5) { Random.nextInt().toShort() }

        AL.newAudioDevice(sampleRate, 1).writeSamples(samples, 0, samples.size)
    }
}
