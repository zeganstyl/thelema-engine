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

package app.thelema.js.audio

import app.thelema.audio.*
import app.thelema.fs.IFile

/** @author zeganstyl */
class JsAL: IAudio {
    val context = AudioContext()

    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice =
        JsAudioDevice(this, samplingRate, channelsNum)

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder {
        TODO("Not yet implemented")
    }

    override fun newSound(file: IFile): ISoundLoader = JsSoundLoader(this, file.path)

    override fun newMusic(file: IFile): IMusic = JsSoundLoader(this, file.path)

    override fun getVersion(param: Int): String = "Web Audio API"

    override fun update() {}

    override fun destroy() {
        context.close()
    }
}
