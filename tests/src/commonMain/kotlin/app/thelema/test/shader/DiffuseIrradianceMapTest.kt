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
import app.thelema.g3d.SimpleSkybox
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.img.CubeFrameBuffer
import app.thelema.test.Test

/** [Tutorial](https://learnopengl.com/PBR/IBL/Diffuse-irradiance)
 *
 * [Sources](https://github.com/JoeyDeVries/LearnOpenGL/tree/master/src/6.pbr/2.1.2.ibl_irradiance)
 *
 * @author zeganstyl */
class DiffuseIrradianceMapTest: Test {
    override val name: String
        get() = "Diffuse irradiance map"

    override fun testMain() {
        val irradianceSkybox = SimpleSkybox(
            """
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
}"""
        )

        irradianceSkybox.shader["texture"] = 0

        irradianceSkybox.texture.load(
            px = "clouds1/clouds1_px.jpg",
            nx = "clouds1/clouds1_nx.jpg",
            py = "clouds1/clouds1_py.jpg",
            ny = "clouds1/clouds1_ny.jpg",
            pz = "clouds1/clouds1_pz.jpg",
            nz = "clouds1/clouds1_nz.jpg"
        )

        val frameBuffer = CubeFrameBuffer(32)

        frameBuffer.renderCube {
            irradianceSkybox.render()
        }

        val skybox = SimpleSkybox()
        skybox.texture = frameBuffer.texture

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            skybox.render()
        }
    }
}