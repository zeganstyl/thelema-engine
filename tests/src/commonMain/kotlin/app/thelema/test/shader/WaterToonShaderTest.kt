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
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_LINEAR_MIPMAP_LINEAR
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

class WaterToonShaderTest: Test {
    override val name: String
        get() = "Water 2D"

    override fun testMain() {
        // https://github.com/McNopper/OpenGL/blob/master/Example15/shader/WaterTexture.frag.glsl

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
varying vec2 vUv;
uniform mat4 worldMatrix;
uniform mat4 projViewMatrix;
uniform float time;

uniform vec3 cameraPos;
varying vec3 viewVector;

void main() {
    vUv = UV;
    
    vec4 pos = worldMatrix * vec4(POSITION, 1.0);
    
    float t = time;
    pos.y += 0.05 * (cos(0.5*t+10.0*vUv.x) + sin(0.5*t+10.0*vUv.y));
    
    viewVector = pos.xyz - cameraPos;
    
    gl_Position = projViewMatrix * pos;
}""",
            fragCode = """
varying vec2 vUv;

uniform sampler2D map;
uniform vec3 basecolor;
uniform vec3 foamcolor;
uniform float time;

varying vec3 viewVector;
uniform samplerCube envTex;
uniform sampler2D normalTex;
 
void main() {
    gl_FragColor.a = 0.7;
    
    vec3 color = texture2D( map,
      vUv * 10.0 +
      0.5*vec2(
        cos(time*0.1),
        sin(time*0.1)
      ) +
      0.1*vec2(
        cos(time*0.012+3.2*10.0*vUv.x),
        sin(time*0.01+3.0*10.0*vUv.y)
      )
    ).rgb;
    
    vec3 color2 = texture2D( map,
      vUv * 13.0 +
      0.8*vec2(
        cos(time*0.1),
        sin(time*0.1)
      ) +
      0.01*vec2(
        cos(1.7 + time*0.12+3.2*10.0*vUv.x),
        sin(1.7 + time*0.1+3.0*10.0*vUv.y)
      )
    ).rgb;
    
    gl_FragColor.rgb = mix(basecolor * clamp(1.0 - color2, 0.9, 1.0), foamcolor, color.r);
}
""")

        var time = 0f

        shader.bind()
        shader.set("basecolor", 0.1f, 0.4f, 0.5f)
        shader.set("foamcolor", 0.7f, 0.7f, 0.7f)
        shader["time"] = time
        shader["map"] = 1
        shader["normalTex"] = 2

        val foamTex = Texture2D()
        foamTex.load("water-foam.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, generateMipmaps = true)

        val normalTex = Texture2D()
        normalTex.load("NormalMap.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, generateMipmaps = true)

        GL.isDepthTestEnabled = true

        val mesh = PlaneMeshBuilder(10f, 10f, 100, 100).apply {
            positionName = "POSITION"
            uvName = "UV"
            uv = true
        }.build()

        ActiveCamera {
            lookAt(Vec3(1f, 3f, 1f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val control = OrbitCameraControl(camera = ActiveCamera)
        control.listenToMouse()

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

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.setSimpleAlphaBlending()
        GL.isBlendingEnabled = true
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val delta = APP.deltaTime

            control.update(delta)
            ActiveCamera.updateCamera()

            time += delta

            shader.bind()
            shader["time"] = time
            shader["projViewMatrix"] = ActiveCamera.viewProjectionMatrix
            shader["worldMatrix"] = cubeMatrix4
            shader["cameraPos"] = ActiveCamera.position

            foamTex.bind(1)

            putParams(0, 1f, 1f, 0.5f, 0.5f, 0.5f)
            putParams(1, 0.5f, 0.02f, 3f, 1f, 0f)
            putParams(2, 0.1f, 0.015f, 2f, -0.1f, -0.2f)
            putParams(3, 1.1f, 0.008f, 1f, -0.2f, -0.1f)

            mesh.render(shader)
        }
    }
}