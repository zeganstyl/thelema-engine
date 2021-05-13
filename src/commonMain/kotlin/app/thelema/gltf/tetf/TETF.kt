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

package app.thelema.gltf.tetf

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.gltf.*
import app.thelema.json.IJsonObject

/** Thelema Engine Transmission Format.
 *
 * @author zeganstyl */
class TETF: IEntityComponent, IGLTFExtension {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            gltf = value?.component() ?: GLTF()
        }

    override val componentName: String
        get() = "TETF"

    var gltf: GLTF = GLTF()

    val shaders: IGLTFArray = GLTFArray("shaders", gltf) { TETFShader(it) }
    val libraries: IGLTFArray = GLTFArray("libraries", gltf) { TETFLibrary(it) }
    val entities: IGLTFArray = GLTFArray("entities", gltf) { TETFEntity(it) }

    override val name: String
        get() = componentName

    override var currentProgress: Long = 0
    override var maxProgress: Long = 1
    override var jsonOrNull: IJsonObject? = null

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    override fun updateProgress() {}

    override fun readJson() {
        json.obj("extra") {
            array("shaders") {
                objs {
                    shaders.addElement(this)
                }
            }

            array("libraries") {
                objs {
                    libraries.addElement(this)
                }
            }
        }

        currentProgress++
    }

    override fun writeJson() {
        json.setObj("extra") {
            if (shaders.isNotEmpty()) {
                setArray("shaders") {
                    for (i in shaders.indices) {
                        add(shaders[i].json)
                    }
                }
            }

            if (libraries.isNotEmpty()) {
                setArray("libraries") {
                    for (i in libraries.indices) {
                        add(libraries[i].json)
                    }
                }
            }

            if (entities.isNotEmpty()) {
                setArray("entities") {
                    entities.jsonOrNull = this
                    entities.writeJson()
                }
            }
        }
    }

    override fun destroy() {
        super.destroy()
    }
}
