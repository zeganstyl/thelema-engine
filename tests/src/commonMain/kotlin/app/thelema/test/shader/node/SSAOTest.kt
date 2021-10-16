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
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.material
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.img.GBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.BUTTON
import app.thelema.gltf.GLTF
import app.thelema.img.renderCurrentScene
import app.thelema.input.MOUSE
import app.thelema.shader.PBRShader
import app.thelema.shader.post.SSAO
import app.thelema.test.g3d.gltf.GLTFTestBase

/** @author zeganstyl */
class SSAOTest: GLTFTestBase("nightshade/nightshade.gltf") {
    override val name: String
        get() = "SSAO"

    override fun loaded(mainScene: IEntity, gltf: GLTF) {
        super.loaded(mainScene, gltf)

        animate("anim")

        mainScene.entity("plane") {
            component<PlaneMesh> { setSize(5f) }
            material {
                shader = PBRShader(true) {}
            }
        }
    }

    override fun testMain() {
        GLTF.defaultConf {
            setupGBufferShader = true
            shaderVersion = 330
        }

        super.testMain()

        val gBuffer = GBuffer()

        val ssao = SSAO(gBuffer.colorMap, gBuffer.normalMap, gBuffer.positionMap)

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                if (button == BUTTON.LEFT) ssao.visualizeSsao = !ssao.visualizeSsao
            }
        })

        APP.onRender = {
            gBuffer.renderCurrentScene()
            ssao.render(null)
        }
    }
}