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

package app.thelema.test.shader

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.*
import app.thelema.gl.*
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.img.DepthFrameBuffer
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.Color

/** [learnopengl.com](https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping)
 *
 * @author zeganstyl*/
class ShadowMappingBaseTest: Test {
    override val name: String
        get() = "Shadow mapping base"

    override fun testMain() {
        val sceneObjectShader = Shader(
            vertCode = """
attribute vec3 POSITION;
uniform vec3 pos;
uniform mat4 viewProj;

uniform mat4 lightMatrix;

varying vec4 lightSpacePos;

void main() {
    vec4 worldPos = vec4(POSITION + pos, 1.0);
    lightSpacePos = lightMatrix * worldPos;
    gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            fragCode = """
uniform vec4 color;
uniform sampler2D shadowMap;

varying vec4 lightSpacePos;

float CalcShadowFactor(vec4 LightSpacePos) {
    // convert to NDC space
    vec3 projCoords = LightSpacePos.xyz / LightSpacePos.w;
    
    if (projCoords.z > 1.0)
        return 0.0;
    
    projCoords = projCoords * 0.5 + 0.5;

    float currentDepth = projCoords.z;
    float closestDepth = texture2D(shadowMap, projCoords.xy).x;

    float bias = 0.001;
    if (currentDepth - bias > closestDepth)
        return 1.0;
    else
        return 0.0;
}

void main () {
    float ShadowFactor = CalcShadowFactor(lightSpacePos);
    gl_FragColor = vec4(color.xyz * (1.0 - ShadowFactor), color.a);
}""")

        sceneObjectShader.bind()
        sceneObjectShader["shadowMap"] = 0


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
            far = 500f
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
        lightMatrix.mul(Mat4().setToLook(
            position = Vec3(50f, 50f, 50f),
            direction = Vec3(-50f, -50f, -50f).nor(),
            up = Vec3(0f, 1f, 0f)
        ))

        val cube = BoxMesh { setSize(2f) }
        val plane = PlaneMesh { setSize(500f) }

        // visualize light field bounds
        val lightFrustumMesh = FrustumMesh(Mat4().set(lightMatrix).inv())

        val cubeColor = Vec4(1f, 0.5f, 0f, 1f)

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
                GL.cullFaceMode = GL_FRONT

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

                GL.cullFaceMode = GL_BACK
            }

            val shader = sceneObjectShader
            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
            shader["lightMatrix"] = lightMatrix
            depthBuffer.getTexture(0).bind(0)

            // render plane
            shader.set("pos", 0f, 0f, 0f)
            shader.set("color", 1f, 1f, 1f, 1f)
            plane.render(shader)

            // render cubes
            shader["color"] = cubeColor
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

            // render light frustum
            shader.setColor("color", Color.GREEN)
            shader.set("pos", 0f, 0f, 0f)
            lightFrustumMesh.render(shader)

            // render light depth map
            ScreenQuad.render(1, maxCellSize = 0.2f, padding = 0.01f) { depthBuffer.getTexture(0) }
        }
    }
}
