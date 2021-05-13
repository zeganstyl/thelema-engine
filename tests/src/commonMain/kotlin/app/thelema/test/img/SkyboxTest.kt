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
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.TextureCube
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class SkyboxTest: Test {
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

        shader["texture"] = 0

        val textureCube = TextureCube()

        textureCube.load(
            positiveX = "clouds1/clouds1_east.jpg",
            negativeX = "clouds1/clouds1_west.jpg",
            positiveY = "clouds1/clouds1_up.jpg",
            negativeY = "clouds1/clouds1_down.jpg",
            positiveZ = "clouds1/clouds1_north.jpg",
            negativeZ = "clouds1/clouds1_south.jpg"
        )

        val mesh = BoxMeshBuilder(
            xSize = 0.5f,
            ySize = 0.5f,
            zSize = 0.5f
        ).build()

        val control = OrbitCameraControl(camera = ActiveCamera)
        control.listenToMouse()
        LOG.info(control.help)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
            shader["camFar"] = ActiveCamera.far
            shader["camPos"] = ActiveCamera.position
            textureCube.bind(0)
            mesh.render(shader)

            // Don't forget about depth. Skybox depth is not needed for other geometries
            GL.glClear(GL_DEPTH_BUFFER_BIT)
        }
    }
}