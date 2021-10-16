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

package app.thelema.test.g3d.gltf

import app.thelema.anim.AnimationPlayer
import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.scene
import app.thelema.gl.GL
import app.thelema.gl.glClearColor
import app.thelema.gltf.GLTF
import app.thelema.gltf.gltf
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.res.load
import app.thelema.test.Test
import app.thelema.utils.Color

abstract class GLTFTestBase(val uri: String, val orbitCameraControl: Boolean = true): Test {
    override val name: String
        get() = ""

    val modelName = uri.substringAfterLast('/').substringBeforeLast('.')

    fun animate(name: String, loop: Boolean = true, speed: Float = 1f) {
        mainScene.entity(modelName).component<AnimationPlayer> {
            setAnimation(name, if (loop) -1 else 1, speed = speed)
        }
    }

    val mainScene = Entity()

    open fun configure(gltf: GLTF) {}

    open fun loaded(mainScene: IEntity, gltf: GLTF) {
        mainScene.addEntity(gltf.scene.copyDeep(modelName))
    }

    override fun testMain() {
        glClearColor(Color.SKY)

        ActiveCamera {
            lookAt(Vec3(0f, 0f, -15f), Vec3(0f, 0f, 0f))
        }

        mainScene.apply {
            makeCurrent()

            scene()

            if (orbitCameraControl) orbitCameraControl()

            entity("light") {
                directionalLight {
                    setDirectionFromPosition(1f, 1f, 1f)
                    intensity = 1f
                }
            }
        }

        RES.gltf(uri) {
            configure(this)
            onLoaded {
                loaded(mainScene, this)
            }
        }
    }
}