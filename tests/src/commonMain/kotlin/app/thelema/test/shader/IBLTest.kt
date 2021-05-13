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
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_RGBA
import app.thelema.img.CubeFrameBuffer
import app.thelema.img.TextureCube
import app.thelema.math.IMat4
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class IBLTest: Test {
    override val name: String
        get() = "IBL"

    override fun testMain() {

        val skyboxVert = """
attribute vec3 POSITION;
varying vec3 vPosition;

uniform mat4 viewProj;
uniform vec3 camPos;
uniform float camFar;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION * camFar + camPos, 1.0);
}"""


        val simpleSkyboxShader = Shader(
            vertCode = skyboxVert,
            fragCode = """
varying vec3 vPosition;
uniform samplerCube texture;

void main () {
    gl_FragColor = textureCube(texture, vPosition);
}""")

        simpleSkyboxShader["texture"] = 0


        val irradianceShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;
uniform mat4 viewProj;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec3 vPosition;
uniform samplerCube texture;

const float PI = 3.14159265359;

void main () {
    // the sample direction equals the hemisphere's orientation 
    vec3 normal = normalize(vPosition);

    vec3 irradiance = vec3(0.0);

    vec3 up    = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, normal);
    up         = cross(normal, right);

    float sampleDelta = 0.025;
    float nrSamples = 0.0; 
    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {
        for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta) {
            // spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * normal; 

            irradiance += textureCube(texture, sampleVec).rgb * cos(theta) * sin(theta);
            nrSamples++;
        }
    }
    irradiance = PI * irradiance * (1.0 / float(nrSamples));

    gl_FragColor = vec4(irradiance, 1.0);
}""")

        val irradianceBuffer = CubeFrameBuffer(
            width = 512,
            height = 512,
            pixelFormat = GL_RGBA
        )
        irradianceBuffer.checkErrors()

        irradianceShader["texture"] = 0

        val textureCube = TextureCube()

        textureCube.load(
            positiveX = "clouds1/clouds1_east.jpg",
            negativeX = "clouds1/clouds1_west.jpg",
            positiveY = "clouds1/clouds1_up.jpg",
            negativeY = "clouds1/clouds1_down.jpg",
            positiveZ = "clouds1/clouds1_north.jpg",
            negativeZ = "clouds1/clouds1_south.jpg"
        )

        val skyboxMesh = BoxMeshBuilder(
            xSize = 0.5f,
            ySize = 0.5f,
            zSize = 0.5f
        ).build()

        val control = OrbitCameraControl()
        control.listenToMouse()
        LOG.info(control.help)

        //GL.isDepthTestEnabled = true

        val captureProjection = Mat4().setToProjection(0.1f, 10f, 90f, 1f)
        val captureViews: Array<IMat4> = arrayOf(
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(1f, 0f, 0f), Vec3(0f, -1f, 0f))),
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(-1f, 0f, 0f), Vec3(0f, -1f, 0f))),
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(0f, 1f, 0f), Vec3(0f, 0f, 1f))),
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(0f, -1f, 0f), Vec3(0f, 0f, -1f))),
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(0f, 0f, 1f), Vec3(0f, -1f, 0f))),
            Mat4().set(captureProjection).mul(Mat4().setToLook(MATH.Zero3, Vec3(0f, 0f, -1f), Vec3(0f, -1f, 0f)))
        )

        irradianceShader.bind()
        textureCube.bind(0)

        irradianceBuffer.renderCube {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            //GL.glViewport(0, 0, 32, 32)
            irradianceShader["viewProj"] = captureViews[it]
            skyboxMesh.render(irradianceShader)
        }

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            simpleSkyboxShader.bind()
            simpleSkyboxShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            simpleSkyboxShader["camFar"] = ActiveCamera.far
            simpleSkyboxShader["camPos"] = ActiveCamera.position
            irradianceBuffer.texture.bind(0)
            skyboxMesh.render(simpleSkyboxShader)
        }
    }
}