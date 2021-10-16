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
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.FrustumMesh
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import app.thelema.utils.Color

/** @author zeganstyl */
class FrustumMeshTest: Test {
    override val name: String
        get() = "Frustum Mesh Builder"

    override fun testMain() {
        val shader = SimpleShader3D {
            setupOnlyColor(Color.WHITE)
        }

        val perspectiveCamera = Camera {
            near = 0.1f
            far = 1f
            isOrthographic = false
            updateCamera()
        }

        val orthographicCamera = Camera {
            isOrthographic = true
            viewportWidth = 1f
            viewportHeight = 1f
            near = 0.1f
            far = 1f
            updateCamera()
        }

        val perspectiveFrustumMesh = FrustumMesh(perspectiveCamera.inverseViewProjectionMatrix)
        val orthographicFrustumMesh = FrustumMesh(orthographicCamera.inverseViewProjectionMatrix)

        val control = OrbitCameraControl {
            targetDistance = 3f
            azimuth = 0.5f
        }

        APP.onRender = {
            control.updateNow()

            shader.color?.set(Color.ORANGE)
            perspectiveFrustumMesh.render(shader)

            shader.color?.set(Color.GREEN)
            orthographicFrustumMesh.render(shader)
        }
    }
}
