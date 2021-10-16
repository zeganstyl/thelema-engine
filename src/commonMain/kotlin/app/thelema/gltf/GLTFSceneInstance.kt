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

import app.thelema.ecs.ComponentAdapter
import app.thelema.ecs.EntityListener
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent

class GLTFSceneInstance: ComponentAdapter() {
    override val componentName: String
        get() = "GLTFSceneInstance"

    var enabled = true
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    model?.scene?.addEntityListener(entityListener)
                } else {
                    model?.scene?.removeEntityListener(entityListener)
                }
            }
        }

    override var entityOrNull: IEntity?
        get() = super.entityOrNull
        set(value) {
            super.entityOrNull = value
            model?.also { value?.setDeep(it.scene) }
        }

    var model: GLTF? = null
        set(value) {
            if (field != value) {
                field?.scene?.removeEntityListener(entityListener)
                field = value
                if (enabled && value != null) {
                    value.onLoaded {
                        val scene = (value.scenes[value.mainSceneIndex] as GLTFScene)
                        scene.loader.load()
                        scene.scene.addEntityListener(entityListener)
                        val entity = entity.entity("Scene")
                        scene.scene.copyDeep(to = entity)
                        entity.name = "Scene"
                        entity.serializeEntity = false
                    }
                    value.load()
                }
            }
        }

    private val entityListener = object : EntityListener {
        override fun addedComponentToBranch(component: IEntityComponent) {
            if (enabled) {
                model?.scene?.also {
                    val path = it.getRelativePathTo(component.entity)
                    entity.entity("Scene").makePath(path).component(component.componentName).setComponent(component)
                }
            }
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (enabled) {
                model?.scene?.also {
                    val path = it.getRelativePathTo(component.entity)
                    entity.entity("Scene").getEntityByPath(path)?.removeComponent(component.componentName)
                }
            }
        }
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        super.addedComponentToBranch(component)
        if (component is GLTFSceneInstance && component != this) {
            component.enabled = false
        }
    }
}