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
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import app.thelema.test.g3d.terrain.TerrainLodFrame2to1MeshTest
import app.thelema.test.g3d.terrain.TerrainMeshWithLoDTest
import app.thelema.test.gl.AlphaBlendingTest
import app.thelema.test.gl.InstancingTest
import app.thelema.test.img.*
import app.thelema.test.phys.ObjectShotTest
import app.thelema.test.phys.RayShapeTest
import app.thelema.test.phys.TrimeshShapeTest
import app.thelema.test.shader.IBLTest
import app.thelema.test.shader.LogarithmicDepthBufferTest
import app.thelema.test.shader.node.SkyboxShaderNodeTest
import app.thelema.test.ui.*

class MainTest {
    init {
        APP.setupPhysicsComponents()

        SkyboxShaderNodeTest().testMain()
    }
}
