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

import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.fs.projectFile
import app.thelema.json.IJsonObject
import app.thelema.json.JSON
import app.thelema.res.LoaderAdapter
import app.thelema.res.RES
import app.thelema.utils.LOG

class EntityLoader: LoaderAdapter() {
    val targetEntity: IEntity = Entity()

    override val componentName: String
        get() = "EntityLoader"

    override fun loadBase(file: IFile) {
        file.readText {
            try {
                val json = JSON.parseObject(it)
                targetEntity.readJson(json)
            } catch (ex: Exception) {
                LOG.error("EntityLoader: can't load entity \"${file.path}\"")
                ex.printStackTrace()
            }
        }

        currentProgress = 1
        stop()
    }

    override fun getOrCreateFile(): IFile? {
        if (file == null) {
            val fileName = entity.name + ext
            file = projectFile(fileName)
        }
        return file
    }

    fun saveTargetEntity() {
        getOrCreateFile()?.writeText(JSON.printObject(targetEntity))
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)
        if (targetEntity.name.isNotEmpty()) saveTargetEntity()
    }

    companion object {
        const val ext = ".entity"
    }
}

inline fun IEntity.entityLoader(block: EntityLoader.() -> Unit) = component(block)
inline fun IEntity.entityLoader() = component<EntityLoader>()
