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

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.gltf.GLTF
import app.thelema.res.load


/** [Description](https://github.com/KhronosGroup/glTF-Sample-Models/tree/master/2.0/MultiUVTest)
 *
 * @author zeganstyl */
class GLTFDamagedHelmetTest: GLTFTestBase("gltf/house_disaster/scene.gltf") {
    override fun loaded(mainScene: IEntity, gltf: GLTF) {
        super.loaded(mainScene, gltf)
        mainScene.orbitCameraControl()
    }
}
