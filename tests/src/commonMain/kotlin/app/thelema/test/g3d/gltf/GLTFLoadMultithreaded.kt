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

package app.thelema.test.g3d.gltf

import app.thelema.ecs.Entity
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.scene
import app.thelema.gl.glClearColor
import app.thelema.gltf.gltf
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.test.Test
import app.thelema.utils.Color

class GLTFLoadMultithreaded: Test {
    override val name: String
        get() = "glTF load multithreaded"

    override fun testMain() {
        ActiveCamera {
            lookAt(Vec3(1f, 1f, 1f), Vec3(0f, 0f, 0f))
            near = 0.01f
            far = 1000f
        }

        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity {
                directionalLight {
                    setDirectionFromPosition(1f, 1f, 1f)
                }
            }

            RES.loadOnSeparateThreadByDefault = true

            RES.gltf("nightshade/nightshade.gltf") {
                onLoaded {
                    addEntity(scene.copyDeep("model1"))
                }
            }

            RES.gltf("gltf/DamagedHelmet.glb") {
                onLoaded {
                    addEntity(scene.copyDeep("model2"))
                }
            }
        }

        glClearColor(Color.SKY)
    }
}