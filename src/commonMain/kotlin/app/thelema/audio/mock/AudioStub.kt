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


package app.thelema.audio.mock

import app.thelema.audio.*
import app.thelema.fs.IFile

/** The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
class AudioStub : IAudio {
    override fun newAudioDevice(samplingRate: Int, channelsNum: Int): IAudioDevice =
        AudioDeviceStub()

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): IAudioRecorder =
        AudioRecorderStub()

    override fun newSound(file: IFile): ISound = SoundStub()

    override fun newMusic(file: IFile): IMusic = MusicStub()

    override fun getVersion(param: Int): String = ""

    override fun update() {}
    override fun destroy() {}
}