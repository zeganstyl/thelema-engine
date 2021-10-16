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

package app.thelema.test.img

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.gl.*
import app.thelema.img.DepthFrameBuffer
import app.thelema.img.render
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

class DepthFrameBufferTest: Test {
    override fun testMain() {
        val depthRenderShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform vec3 pos;
uniform mat4 viewProj;

void main() {
    vec4 worldPos = vec4(POSITION + pos, 1.0);
    gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            // fragment shader can be empty for depth rendering
            fragCode = "void main() {}"
        )

        ActiveCamera {
            near = 0.1f
            far = 100f
        }

        val depthBuffer = DepthFrameBuffer(1024, 1024)

        val lightMatrix = Mat4()
        val halfWidth = 50f
        val halfHeight = 50f
        lightMatrix.setToOrtho(
            left = -halfWidth,
            right = halfWidth,
            bottom = -halfHeight,
            top = halfHeight,
            near = 0.1f,
            far = 100f
        )
        lightMatrix.mul(
            Mat4().setToLook(
            position = Vec3(50f, 50f, 50f),
            direction = Vec3(-50f, -50f, -50f).nor(),
            up = Vec3(0f, 1f, 0f)
        ))

        val cube = BoxMesh { setSize(2f) }
        val plane = PlaneMesh { setSize(500f) }

        val control = OrbitCameraControl {
            zenith = 1f
            azimuth = 0f
            target = Vec3(10f, 3f, 0f)
            targetDistance = 10f
        }

        val cubesStartX = -100f
        val cubesEndX = 100f
        val cubesStepX = 20f

        val cubesStartZ = -100f
        val cubesEndZ = 100f
        val cubesStepZ = 20f

        val cubesY = 1f

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            depthBuffer.render {
                val shader = depthRenderShader
                shader.bind()
                shader["viewProj"] = lightMatrix

                // render plane
                shader.set("pos", 0f, 0f, 0f)
                plane.render(shader)

                // render cubes
                var xi = cubesStartX
                while (xi < cubesEndX) {
                    var zi = cubesStartZ
                    while (zi < cubesEndZ) {
                        shader.set("pos", xi, cubesY, zi)
                        cube.render(shader)
                        zi += cubesStepZ
                    }
                    xi += cubesStepX
                }
            }

            ScreenQuad.render(depthBuffer.getTexture(0))
        }
    }
}