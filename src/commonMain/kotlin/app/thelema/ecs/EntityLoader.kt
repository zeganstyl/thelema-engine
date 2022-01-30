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

import app.thelema.fs.IFile
import app.thelema.fs.projectFile
import app.thelema.json.IJsonObject
import app.thelema.json.JSON
import app.thelema.res.IProject
import app.thelema.res.LoaderAdapter
import app.thelema.res.load
import app.thelema.utils.LOG

/** Loads entity from file.
 * Loaded entity will be [targetEntity]. */
class EntityLoader: LoaderAdapter() {
    /** Root scene entity. You can load it with [load] */
    val targetEntity: IEntity = Entity()

    override val componentName: String
        get() = "EntityLoader"

    var saveTargetEntityOnWrite = false

    override fun loadBase(file: IFile) {
        loadEntityTo(targetEntity, file)

        currentProgress = 1
        stop()
    }

    fun loadEntityTo(entity: IEntity, file: IFile) {
        file.readText {
            try {
                val json = JSON.parseObject(it)
                entity.readJson(json)
            } catch (ex: Exception) {
                LOG.error("EntityLoader: can't load entity \"${file.path}\"")
                ex.printStackTrace()
            }
        }
    }

    override fun getOrCreateFile(): IFile? {
        if (file == null) {
            val fileName = entity.name + ext
            file = projectFile(fileName)
        }
        return file
    }

    fun saveTargetEntity() {
        if (targetEntity.name.isEmpty()) targetEntity.name = entityOrNull?.name ?: ""
        if (targetEntity.name.isNotEmpty()) {
            val file = getOrCreateFile()
            if (file != null) {
                file.writeText(JSON.printObject(targetEntity))
            } else {
                LOG.error("$path: Can't save entity, file is null")
            }
        } else {
            LOG.error("$path: Can't save entity, entity name is empty")
        }
    }

    companion object {
        const val ext = ".entity"
    }
}

inline fun IEntity.entityLoader(block: EntityLoader.() -> Unit) = component(block)
inline fun IEntity.entityLoader() = component<EntityLoader>()
fun IProject.loadEntity(uri: String, block: EntityLoader.() -> Unit = {}): EntityLoader = load(uri, block)