/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test.mesh


import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.mesh.gen.SphereMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class SphereMeshBuilderTest: Test {
    override val name: String
        get() = "Sphere Mesh Builder"

    override fun testMain() {

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
uniform mat4 viewProj;
varying vec2 uv;

void main() {
    uv = UV;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 0.0, 1.0);
}"""
        )

        val mesh = SphereMeshBuilder().apply {
            radius = 2f
            hDivisions = 16
            vDivisions = 16
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        val camera = Camera()
        val control = OrbitCameraControl(camera = camera)
        control.listenToMouse()
        LOG.info(control.help)

        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            camera.update()

            shader.bind()
            shader["viewProj"] = camera.viewProjectionMatrix

            mesh.render(shader)
        }
    }
}
