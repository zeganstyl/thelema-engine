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

import app.thelema.fs.IFile

object OpenALCodecs {
    /** Key - file extension in lowercase, value - sound object builder */
    val sound = HashMap<String, (audio: OpenAL, file: IFile) -> OpenALSound>()

    /** Key - file extension in lowercase, value - music object builder */
    val music = HashMap<String, (audio: OpenAL, file: IFile) -> OpenALMusic>()

    init {
        sound["wav"] = { audio, file -> WavSoundLoader(audio, file) }
        music["wav"] = { audio, file -> WavMusic(audio, file) }

        sound["ogg"] = { audio, file -> OggSoundLoader(audio, file) }
        music["ogg"] = { audio, file -> OggMusic(audio, file) }
    }
}