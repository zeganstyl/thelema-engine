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

package app.thelema.test.g3d

import app.thelema.ecs.Entity
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.scene
import app.thelema.g3d.simpleSkybox
import app.thelema.test.Test

class SimpleSkyboxTest: Test {
    override val name: String
        get() = "Skybox"

    override fun testMain() {
        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()
            simpleSkybox {
                texture.load(
                    px = "clouds1/clouds1_px.jpg",
                    nx = "clouds1/clouds1_nx.jpg",
                    py = "clouds1/clouds1_py.jpg",
                    ny = "clouds1/clouds1_ny.jpg",
                    pz = "clouds1/clouds1_pz.jpg",
                    nz = "clouds1/clouds1_nz.jpg"
                )
            }
        }
    }
}