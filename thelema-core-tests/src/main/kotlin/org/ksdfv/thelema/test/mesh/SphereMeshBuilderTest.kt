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

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.mesh.build.SphereMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class SphereMeshBuilderTest: Test {
    override val name: String
        get() = "Sphere Mesh Builder"

    override fun testMain() {
        @Language("GLSL")
        val shader = Shader(
            vertCode = """
attribute vec3 aPosition;
attribute vec2 aUV;
uniform mat4 viewProj;
varying vec2 uv;

void main() {
    uv = aUV;
    gl_Position = viewProj * vec4(aPosition, 1.0);
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
        }.build()

        ActiveCamera.api = Camera()
        val control = OrbitCameraControl()
        control.listenToMouse()
        LOG.info(control.help)

        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.update()

            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix

            mesh.render(shader)
        }
    }
}
