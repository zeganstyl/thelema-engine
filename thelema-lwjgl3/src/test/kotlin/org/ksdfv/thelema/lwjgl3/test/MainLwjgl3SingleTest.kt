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

package org.ksdfv.thelema.lwjgl3.test

import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.test.shaders.CascadedShadowMappingBaseTest

/** @author zeganstyl */
object MainLwjgl3SingleTest {
    @JvmStatic
    fun main(args: Array<String>) {
        Lwjgl3App(Lwjgl3AppConf(windowWidth = 1280, windowHeight = 720)) { CascadedShadowMappingBaseTest().testMain() }
    }
}
