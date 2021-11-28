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

package app.thelema.studio.widget.component

import app.thelema.ecs.*
import app.thelema.fs.IFile
import app.thelema.fs.projectFile
import app.thelema.gltf.GLTF
import app.thelema.res.ILoader
import app.thelema.res.Project
import app.thelema.res.RES
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class ProjectPanel: ComponentPanel<Project>(Project::class) {
    var newResources = 0

    init {
        content.add(TextButton("Find resources") {
            onClick {
                newResources = 0
                findResources(projectFile(""))
                Studio.showStatus("New resources: $newResources")
            }
        })
    }

    private inline fun <reified T: ILoader> makeResourcePath(file: IFile) {
        if (RES.entity.getEntityByPath(file.path)?.componentOrNull<T>() == null) {
            RES.entity.makePath(file.path).component<T>().file = file
            newResources++
        }
    }

    fun findResources(directory: IFile) {
        directory.list().forEach {
            if (it.isDirectory && !it.name.startsWith('.')) {
                RES.entity.makePath(it.path)
                findResources(it)
            } else {
                when (it.extension.lowercase()) {
                    EntityLoader.ext -> makeResourcePath<EntityLoader>(it)
                    "gltf", "glb", "vrm" -> makeResourcePath<GLTF>(it)
                }
            }
        }
    }
}