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

package app.thelema
import app.thelema.fs.FileLocation

class GLFWAppConf : GLFWWindowConf() {
    var disableAudio: Boolean = false
    /** The maximum number of threads to use for network requests. Default is [Integer.MAX_VALUE].  */
    var maxNetThreads: Int = Int.MAX_VALUE
    var audioDeviceSimultaneousSources: Int = 16
    var audioDeviceBufferSize: Int = 512
    var audioDeviceBufferCount: Int = 9

    /**Sets the polling rate during idle time in non-continuous rendering mode. Must be positive.
     * Default is 60.  */
    var idleFPS: Int = 60

    var cacheDirectory: String = ".prefs/"
    var cacheFileLocation: String = FileLocation.External
}
