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

package app.thelema.test.shader.node

import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.img.GBuffer
import app.thelema.gl.TextureRenderer
import app.thelema.gltf.GLTF
import app.thelema.img.render
import app.thelema.test.g3d.gltf.GLTFTestBase

/** @author zeganstyl */
class GBufferTest: GLTFTestBase("nightshade/nightshade.gltf") {
    override val name: String
        get() = "G-Buffer"

    override fun testMain() {
        GLTF.defaultConf {
            setupGBufferShader = true
            shaderVersion = 330
        }

        super.testMain()

        val screenQuad = TextureRenderer(0f, 0f, 0.5f, 0.5f)

        val gBuffer = GBuffer()

        APP.onRender = {
            gBuffer.render {
                ECS.render()
            }

            screenQuad.render(gBuffer.colorMap, clearMask = null) {
                screenQuad.setPosition(-0.5f, 0.5f)
            }

            screenQuad.render(gBuffer.normalMap, clearMask = null) {
                screenQuad.setPosition(0.5f, 0.5f)
            }

            screenQuad.render(gBuffer.positionMap, clearMask = null) {
                screenQuad.setPosition(-0.5f, -0.5f)
            }

            screenQuad.render(gBuffer.depthMap, clearMask = null) {
                screenQuad.setPosition(0.5f, -0.5f)
            }
        }
    }
}