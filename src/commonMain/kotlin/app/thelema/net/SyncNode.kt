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

package app.thelema.net

import app.thelema.ecs.DefaultEntitySystem
import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.json.IJsonObject

class SyncNode: IEntityComponent {
    override val componentName: String
        get() = "SyncNode"

    override var entityOrNull: IEntity? = null

    var mode: String = WebSocketSyncMode.All

    val system = ECS.systems.first { it.systemName == "Default" } as DefaultEntitySystem

    var id = system.newSyncId(this)

    var serializationRequested = false

    fun deserialize(json: IJsonObject) {
        json.obj("components") {
            objs {
                entity.component(string(it)).readJson(this)
            }
        }
    }

    fun serialize(json: IJsonObject) {
        when (mode) {
            WebSocketSyncMode.All -> {
                entity.forEachComponent {

                }
            }
        }
    }
}