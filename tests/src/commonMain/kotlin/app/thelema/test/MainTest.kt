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

package app.thelema.test

import app.thelema.app.APP
import app.thelema.test.audio.Sound3DTest
import app.thelema.test.g3d.MeshCubeTest
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import app.thelema.test.g3d.terrain.GrassPatchMeshTest
import app.thelema.test.gl.InstancingTest
import app.thelema.test.gl.MeshTest
import app.thelema.test.gl.TriangleBaseTest
import app.thelema.test.gl.VertexArrayObjectTest
import app.thelema.test.phys.BoxShapeTest
import app.thelema.test.phys.ObjectShotTest
import app.thelema.test.shader.HeightMapToNormalMapTest
import app.thelema.test.ui.LabelTest

class MainTest {
    init {
        APP.setupPhysicsComponents()

        InstancingTest().testMain()
    }
}
