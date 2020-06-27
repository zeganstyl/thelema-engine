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

package org.ksdfv.thelema.audio

import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.audio.mock.MockAudio

/** @author zeganstyl */
object AL: IAL {
    var api: IAL = MockAudio()

    override fun newAudioDevice(samplingRate: Int, isMono: Boolean) = api.newAudioDevice(samplingRate, isMono)
    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean) = api.newAudioRecorder(samplingRate, isMono)
    override fun newSound(file: IFile) = api.newSound(file)
    override fun newMusic(file: IFile) = api.newMusic(file)

    override fun getVersion(param: Int): String = api.getVersion(param)

    override fun destroy() = api.destroy()
}
