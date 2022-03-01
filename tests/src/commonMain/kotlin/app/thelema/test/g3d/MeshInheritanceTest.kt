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
import app.thelema.ecs.component
import app.thelema.g3d.*
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.gl.mesh
import app.thelema.gl.meshInstance
import app.thelema.test.Test
import app.thelema.shader.SimpleShader3D

class MeshInheritanceTest: Test {
    override fun testMain() {
        val box = BoxMesh { setSize(2f) }

        Entity {
            makeCurrent()
            scene()
            component<OrbitCameraControl>()

            entity {
                meshInstance { mesh = box.mesh }
                material { shader = SimpleShader3D() }
            }
        }
    }
}