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
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.img.Texture2D
import app.thelema.img.TextureCube
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

class WaterShaderTest: Test {
    override val name: String
        get() = "Water"

    override fun testMain() {
        // https://github.com/McNopper/OpenGL/blob/master/Example15/shader/WaterTexture.frag.glsl

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
varying vec2 uv;
uniform mat4 worldMatrix;
uniform mat4 projViewMatrix;

uniform vec3 cameraPos;
varying vec3 viewVector;

void main() {
    uv = UV;
    
    vec4 pos = worldMatrix * vec4(POSITION, 1.0);
    viewVector = pos.xyz - cameraPos;
    
    gl_Position = projViewMatrix * pos;
}""",
            fragCode = """
const float pi = 3.14159;
uniform float waterHeight;
uniform float time;
uniform int numWaves;
uniform float amplitude[8];
uniform float wavelength[8];
uniform float speed[8];
uniform vec2 direction[8];
uniform vec3 baseColor;

uniform sampler2D normalTex;
uniform samplerCube envTex;

varying vec2 uv;
varying vec3 viewVector;

void main() {
    vec3 norView = normalize(viewVector);
    
    vec3 normal = texture2D(normalTex, uv * 3.0 +
      0.8*vec2(
        cos(time*0.1),
        sin(time*0.1)
      ) +
      0.001*vec2(
        cos(1.7 + time*1.2+3.2*100.0*uv.x),
        sin(1.7 + time*1.0+3.0*100.0*uv.y)
      )
      ).rgb * 0.5 - 0.5;
    
    vec3 dir = reflect(norView, normal);
    
    gl_FragColor.a = 0.7;
    gl_FragColor.rgb = mix(baseColor, textureCube(envTex, dir).rgb, 0.7);
}
""")

        var time = 0f

        shader.bind()
        shader["envTex"] = 0
        shader["normalTex"] = 1
        shader["time"] = time
        shader["waterHeight"] = 1f
        shader["numWaves"] = 2
        shader["baseColor"] = Vec3(0f, 0.5f, 1f)

        val normalTexture = Texture2D("NormalMap.png")

        val environmentTexture = TextureCube()
        environmentTexture.load(
            px = "clouds1/clouds1_px.jpg",
            nx = "clouds1/clouds1_nx.jpg",
            py = "clouds1/clouds1_py.jpg",
            ny = "clouds1/clouds1_ny.jpg",
            pz = "clouds1/clouds1_pz.jpg",
            nz = "clouds1/clouds1_nz.jpg"
        )

        val mesh = PlaneMesh {
            setSize(10f)
            setDivisions(100)
        }

        ActiveCamera {
            lookAt(Vec3(1f, 3f, 1f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val control = OrbitCameraControl()

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        val overallSteepness = 0.2f
        val NUMBERWAVES = 4

        fun putParams(waveIndex: Int, speed: Float, amplitude: Float, wavelength: Float, x: Float, y: Float) {
            shader["amplitude[$waveIndex]"] = amplitude
            shader["wavelength[$waveIndex]"] = wavelength
            shader["speed[$waveIndex]"] = speed
            shader["direction[$waveIndex].x"] = x
            shader["direction[$waveIndex].y"] = y
        }

        APP.onUpdate = {
            control.update(it)
            ActiveCamera.updateCamera()

            time += it
        }

        APP.onRender = {
            shader.bind()
            shader["time"] = time
            shader["projViewMatrix"] = ActiveCamera.viewProjectionMatrix
            shader["worldMatrix"] = cubeMatrix4
            shader["cameraPos"] = ActiveCamera.position

            environmentTexture.bind(0)
            normalTexture.bind(1)

            putParams(0, 1f, 1f, 0.5f, 0.5f, 0.5f)
            putParams(1, 0.5f, 0.02f, 3f, 1f, 0f)
            putParams(2, 0.1f, 0.015f, 2f, -0.1f, -0.2f)
            putParams(3, 1.1f, 0.008f, 1f, -0.2f, -0.1f)

            mesh.render(shader)
        }
    }
}