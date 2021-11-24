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

package app.thelema.lwjgl3

import app.thelema.fs.FS
import app.thelema.res.openThelemaProject
import app.thelema.test.*
import app.thelema.test.audio.MusicOggTest
import app.thelema.test.g3d.BoxMeshTest
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import app.thelema.test.phys.BoxShapeTest
import app.thelema.test.phys.TrimeshShapeTest

object MainTestJvm {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = JvmApp {
            width = 1280
            height = 720
            msaaSamples = 4
        }

        MainTest()
        //RecastTest().testMain()

        app.startLoop()
    }
}
