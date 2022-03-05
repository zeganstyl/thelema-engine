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
import app.thelema.test.g3d.PlaneMeshTest
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import app.thelema.test.gl.InstancedMeshTest
import app.thelema.test.img.FrameBufferBlitTest
import app.thelema.test.shader.ForwardRenderingPipelineTest
import app.thelema.test.shader.IBLTest
import app.thelema.test.shader.post.FXAATest
import app.thelema.test.ui.LabelTest
import app.thelema.test.ui.NinePatchTest
import app.thelema.test.ui.SpriteTest
import app.thelema.test.ui.UITest
import app.thelema.ui.test.WindowTest

class MainTest {
    init {
        APP.setupPhysicsComponents()

        UITest().testMain()
    }
}
