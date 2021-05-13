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

package app.thelema.ecs

import app.thelema.json.JSON
import app.thelema.res.LoaderAdapter
import app.thelema.utils.LOG

class EntityLoader: LoaderAdapter() {
    override val runOnGLThreadRequest: Boolean
        get() = false

    var targetEntity: IEntity? = null

    override val componentName: String
        get() = "EntityLoader"

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    override fun load() {
        val resource = resource()
        resource.file.readText {
            try {
                val json = JSON.parseObject(it)
                val newEntity = entity.entity(json.string("name", "Entity"))
                newEntity.destroy()
                newEntity.readJson(json)
                targetEntity = newEntity
            } catch (ex: Exception) {
                LOG.error("Error while loading entity ${resource.uri}")
            }
        }
    }
}