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

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.*
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.directionalLight
import app.thelema.gl.GL_FLOAT
import app.thelema.gl.GL_RGB
import app.thelema.gl.GL_RGB16F
import app.thelema.gltf.GLTF
import app.thelema.gltf.gltf
import app.thelema.img.CubeFrameBuffer
import app.thelema.img.Texture2D
import app.thelema.res.RES
import app.thelema.res.load
import app.thelema.shader.EquirectangularToCubemapConverter
import app.thelema.test.Test
import app.thelema.utils.Color

class IBLTest: Test {
    override val name: String
        get() = "IBL"

    override fun testMain() {
        val conv = EquirectangularToCubemapConverter()

        val tex = Texture2D("hdri/eilenriede_park_1k.hdr")

        val skyboxTextureFrameBuffer = CubeFrameBuffer(512, GL_RGB, GL_RGB16F, GL_FLOAT)

        conv.render(tex, skyboxTextureFrameBuffer)

        val skyboxTexture = skyboxTextureFrameBuffer.texture

        val baker = IBLMapBaker()
        baker.generateBrdfLUT()
        baker.environment = skyboxTexture
        baker.render()

        Entity {
            makeCurrent()
            scene {
                world = World().apply {
                    environmentIrradianceMap = baker.irradianceMap
                    environmentPrefilterMap = baker.prefilterMap
                    brdfLUTMap = baker.brdfLUT
                }
            }
            orbitCameraControl()

            entity("skybox") {
                simpleSkybox {
                    texture = skyboxTexture
                }
            }

            entity("light") {
                directionalLight {
                    setDirectionFromPosition(0f, 1f, 0f)
                    color.set(Color.SKY)
                    intensity = 5f
                }
            }

            RES.gltf("gltf/DamagedHelmet.glb") {
                conf.ibl = true
                conf.iblMaxMipLevels = baker.maxMipLevels
                onLoaded {
                    addEntity(scene.copyDeep())
                }
            }
        }
    }
}