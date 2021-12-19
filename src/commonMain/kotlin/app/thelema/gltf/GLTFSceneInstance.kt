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

package app.thelema.gltf

import app.thelema.ecs.*
import app.thelema.g3d.SceneInstance

class GLTFSceneInstance: IEntityComponent {
    override val componentName: String
        get() = "GLTFSceneInstance"

    var provider: SceneInstance = SceneInstance()

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            provider = value?.component() ?: SceneInstance()
            model?.also { provider.sceneClassEntity = it.scene }
        }

    var model: GLTF? = null
        set(value) {
            if (field != value) {
                field = value
                if (value != null) {
                    if (provider.enabled) {
                        value.onLoaded {
                            val scene = (value.scenes[value.mainSceneIndex] as GLTFScene)
                            scene.loader.load()
                            provider.sceneClassEntity = scene.scene
                        }
                    }
                    value.load()
                }
            }
        }

    fun require() {

    }
}