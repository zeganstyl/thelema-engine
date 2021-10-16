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

package app.thelema.res

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.json.IJsonObject

class ResourceHolder: IEntityComponent {
    override val componentName: String
        get() = "ResourceHolder"

    override var entityOrNull: IEntity? = null

    private val loadersInternal = ArrayList<ILoader?>(1)
    val loaders: List<ILoader?>
        get() = loadersInternal

    fun hold(loader: ILoader) {
        if (!loadersInternal.contains(loader)) loadersInternal.add(loader)
        loader.hold(this)
    }

    fun release(loader: ILoader) {
        loader.release(this)
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        loadersInternal.clear()
        json.array("loaders") {
            for (i in 0 until size) {
                loadersInternal.add(null)
            }

            for (i in 0 until size) {
                val e = entityOrNull
                if (e != null) {
                    loadersInternal[i] = e.getRootEntity().makePath(string(i)).component()
                }
            }
        }
        loadersInternal.trimToSize()
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json.setArray("loaders") {
            for (i in loadersInternal.indices) {
                val node = loadersInternal[i]
                if (node != null) add(node.entity.path)
            }
        }
    }
}

fun IEntity.resourceHolder(block: ResourceHolder.() -> Unit) = component(block)
fun IEntity.resourceHolder() = component<ResourceHolder>()