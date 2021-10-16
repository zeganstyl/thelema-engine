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
import app.thelema.g3d.mesh.SkyboxMesh
import app.thelema.gl.GL
import app.thelema.img.TextureCube
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class SkyboxBaseTest: Test {
    override val name: String
        get() = "Skybox (Cube texture)"

    override fun testMain() {
        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;

uniform mat4 viewProj;
uniform vec3 camPos;
uniform float camFar;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION * camFar + camPos, 1.0);
}""",
            fragCode = """
varying vec3 vPosition;
uniform samplerCube texture;

void main () {
    gl_FragColor = textureCube(texture, vPosition);
}""")
        shader.depthMask = false // Skybox depth is not needed for other geometries, so disable writing to depth

        val textureCube = TextureCube(
            px = "clouds1/clouds1_px.jpg",
            nx = "clouds1/clouds1_nx.jpg",
            py = "clouds1/clouds1_py.jpg",
            ny = "clouds1/clouds1_ny.jpg",
            pz = "clouds1/clouds1_pz.jpg",
            nz = "clouds1/clouds1_nz.jpg"
        )

        val box = SkyboxMesh()

        val control = OrbitCameraControl()

        APP.onRender = {
            control.updateNow()

            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
            shader["camFar"] = ActiveCamera.far
            shader["camPos"] = ActiveCamera.position
            textureCube.bind(0)
            box.render(shader)
        }
    }
}