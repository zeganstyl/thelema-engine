import app.thelema.GLFWApp
import app.thelema.GLFWAppConf
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import kotlinx.cinterop.*
import platform.posix.PATH_MAX
import platform.posix.getcwd

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

fun main() {
    val app = GLFWApp(GLFWAppConf().apply {
        windowWidth = 1280
        windowHeight = 720
    })

    val bytes = ByteArray(PATH_MAX)
    getcwd(bytes.refTo(0), PATH_MAX)
    println(bytes.toKString())

    GLTFDamagedHelmetTest().testMain()

    app.startLoop()
}