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

package app.thelema.test

import app.thelema.anim.AnimationPlayer
import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.IScene
import app.thelema.g3d.Scene
import app.thelema.gltf.GLTF
import app.thelema.gltf.GLTFConf
import app.thelema.g3d.light.DirectionalLight
import app.thelema.res.RES

/** Loads model for tests
 *
 * @author zeganstyl */
class GLTFModel(
    var rotate: Boolean = true,
    val conf: GLTFConf = GLTFConf(),
    val response: (model: GLTFModel) -> Unit = {}
) {
    val player: AnimationPlayer = AnimationPlayer()

    val light = DirectionalLight().apply {
        lightIntensity = 1f
    }

    var gltf: GLTF

    val sceneEntity = Entity()
    val scene: IScene = sceneEntity.component()

    val mainScene = Entity { name = "Root" }
    val sceneComponent = mainScene.component<Scene>()

    init {
        sceneEntity.addComponent(light)

        mainScene.component<DirectionalLight>().apply {
            color.set(1f, 0.5f, 0.5f)
            lightIntensity = 5f
            direction.set(-1f, 1f, 1f).nor()
        }

        gltf = RES.loadTyped(
            uri = "robot.vrm",
            block = {
                conf.separateThread = true
                conf.setupVelocityShader = false
                conf.mergeVertexAttributes = false

                onLoaded {
                    val newScene = scene!!.entity.copyDeep()
                    newScene.name = "newScene2"
                    mainScene.addEntity(newScene)
                }
            }
        )
    }

    fun update(delta: Float = APP.deltaTime) {
        player.update(delta)
        sceneComponent.update(delta)
    }

    fun render(shaderChannel: String? = null) {
        sceneComponent.render(shaderChannel)
    }
}