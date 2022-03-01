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

package app.thelema.test.shader.post

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.img.GBuffer
import app.thelema.img.render
import app.thelema.math.Mat4
import app.thelema.shader.Shader
import app.thelema.shader.post.SSAO
import app.thelema.shader.useShader
import app.thelema.test.Test

/** @author zeganstyl */
class SSAOBaseTest: Test {
    override val name: String
        get() = "SSAO Base"

    override fun testMain() {
        val sceneShader = Shader(version = 330,
            vertCode = """
in vec3 POSITION;
in vec2 UV;
in vec3 NORMAL;

uniform mat4 world;
uniform mat4 view;
uniform mat4 viewProj;

out vec2 vUV;
out vec3 vNormal;
out vec3 vViewSpacePosition;

void main() {
vUV = UV;
vNormal = (world * vec4(NORMAL, 1.0)).xyz; // our cube is not translating, only rotating, so we can multiply normals with world matrix
vec4 worldPos = world * vec4(POSITION, 1.0);
vViewSpacePosition = (view * worldPos).xyz; // for ssao positions must in view space
gl_Position = viewProj * worldPos;
}""",
            fragCode = """
in vec2 vUV;
in vec3 vNormal;
in vec3 vViewSpacePosition;

layout (location = 0) out vec4 gColor;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec4 gPosition;

void main() {
gColor = vec4(vUV, 0.0, 1.0);
gNormal = vec4(vNormal, 1.0);
gPosition = vec4(vViewSpacePosition, 1.0);
}""")

        val gBuffer = GBuffer()

        val ssao = SSAO(gBuffer.colorMap, gBuffer.normalMap, gBuffer.positionMap)

        val cube = BoxMesh { setSize(2f) }
        val plane = PlaneMesh { setSize(5f) }

        val cubeMatrix = Mat4()
        val planeMatrix = Mat4()

        APP.onRender = {
            cubeMatrix.rotate(0f, 1f, 0f, APP.deltaTime)

            gBuffer.render {
                sceneShader.useShader {
                    sceneShader["view"] = ActiveCamera.viewMatrix
                    sceneShader["viewProj"] = ActiveCamera.viewProjectionMatrix

                    sceneShader["world"] = cubeMatrix
                    cube.mesh.render()

                    sceneShader["world"] = planeMatrix
                    plane.mesh.render()
                }
            }

            ssao.render(null)
        }
    }
}
