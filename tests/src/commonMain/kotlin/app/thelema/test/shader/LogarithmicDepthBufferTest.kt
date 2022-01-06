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
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test
import kotlin.math.pow

class LogarithmicDepthBufferTest: Test {
    override val name: String
        get() = "Logarithmic depth buffer"

    override fun testMain() {
        // https://outerra.blogspot.com/2009/08/logarithmic-z-buffer.html
        // https://outerra.blogspot.com/2012/11/maximizing-depth-buffer-range-and.html
        // https://outerra.blogspot.com/2013/07/logarithmic-depth-buffer-optimizations.html

        ActiveCamera {
            lookAt(Vec3(0f, 10f, 0f), Vec3(100f, 0f, 0f))
            near = 0.001f // mm
            far = 10f.pow(21)
        }

        val control = OrbitCameraControl {
            azimuth = 3f
            zenith = 1.3f
        }

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 TEXCOORD_0;

varying vec2 uv;
varying float flogz;

const float Fcoef = 2.0 / log2(${ActiveCamera.far} + 1.0);

uniform vec4 posScale;
uniform mat4 viewProj;

void main() {
    uv = TEXCOORD_0;
    gl_Position = viewProj * vec4(POSITION * posScale.w + posScale.xyz, 1.0);
    gl_Position.z = log2(max(1e-6, 1.0 + gl_Position.w)) * Fcoef - ${ActiveCamera.near};
    flogz = 1.0 + gl_Position.w;
}""",
            fragCode = """
varying vec2 uv;
varying float flogz;

const float Fcoef_half = 1.0 / log2(${ActiveCamera.far} + 1.0);

void main() {
    gl_FragDepth = log2(flogz) * Fcoef_half;
    gl_FragColor = vec4(uv, 0.0, 1.0);
}""")

        val plane = PlaneMesh { setSize(10f) }

        val box = BoxMesh { setSize(1f) }

        val units = listOf(
            10f.pow(3), // km
            10f.pow(6), // Mm
            10f.pow(9), // Gm
            10f.pow(12), // Tm
            10f.pow(15), // Em
            10f.pow(18), // Pm
        )

        APP.onRender = {
            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            shader["viewProj"] = ActiveCamera.viewProjectionMatrix

            shader.set("posScale", 0f, 0f, 0f, 1f)
            plane.render(shader)
            box.render(shader)

            for (i in units.indices) {
                val unit = units[i]
                shader.set("posScale", unit, -unit * 0.1f, unit * i * 0.1f, unit * 0.5f)
                plane.render(shader)
                box.render(shader)
            }
        }
    }
}
