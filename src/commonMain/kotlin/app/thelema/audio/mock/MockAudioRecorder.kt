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

import app.thelema.audio.IAudioRecorder

/** The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
class MockAudioRecorder : IAudioRecorder {
    override fun read(samples: ShortArray, offset: Int, numSamples: Int) = Unit
    override fun dispose() = Unit
}