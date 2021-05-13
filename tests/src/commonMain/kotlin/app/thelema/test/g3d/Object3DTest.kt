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

import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.Material
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.test.Test
import app.thelema.math.Vec3
import app.thelema.shader.SimpleShader3D

class Object3DTest: Test {
    override val name: String
        get() = "Object 3D"

    override fun testMain() {
        ActiveCamera {
            lookAt(Vec3(3f, 3f, 3f), Vec3(0f, 0f, 0f))
            updateCamera()
        }

        Entity("Scene") {
            component<Scene>()

            entity("Object") {
                component<Object3D>()
                component<BoxMesh> {
                    setSize(2f)
                    updateMesh()
                }
                component<Material> {
                    shader = SimpleShader3D()
                }
            }

            APP.onRender = { ECS.render(this) }
        }
    }
}