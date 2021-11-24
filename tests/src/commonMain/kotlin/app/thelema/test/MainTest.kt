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
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.shader.SimpleShader3D
import app.thelema.test.g3d.BoxMeshTest
import app.thelema.test.g3d.MeshCubeTest
import app.thelema.test.g3d.gltf.GLTFLoadMultithreaded
import app.thelema.test.gl.MeshTest
import app.thelema.test.gl.ScreenQuadTest
import app.thelema.test.gl.TriangleBaseTest
import app.thelema.test.phys.BoxShapeTest
import app.thelema.test.ui.UITest

class MainTest {
    init {
        //BoxMeshTest().testMain()

        InstancedParticlesTest().testMain()

//        val box = BoxMesh { setSize(2f, 1f, 1f) }
//        val shader = SimpleShader3D()
//        APP.onRender = { shader.render(box.mesh) }
    }
}
