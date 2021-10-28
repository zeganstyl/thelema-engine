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

import app.thelema.ecs.EntityLoader
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.fs.*
import app.thelema.json.JSON
import app.thelema.utils.LOG

class Project: IProject {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (value != null) addedEntityToBranch(value)
        }

    override val componentName: String
        get() = "Project"

    private val loadersInternal = ArrayList<ILoader>()
    override val loaders: List<ILoader>
        get() = loadersInternal

    private val loadingLoaders = ArrayList<ILoader>()
    private val loadingResourcesToRemove = ArrayList<ILoader>()

    override var loadOnSeparateThreadByDefault: Boolean = false

    private val loaderListeners = ArrayList<ProjectListener>()

    override var file: IFile = DefaultProjectFile
        set(value) {
            field = value
            absoluteDirectoryInternal = value.parent()
        }

    override var mainScene: EntityLoader? = null

    private var absoluteDirectoryInternal: IFile = DefaultProjectDirectory
    override val absoluteDirectory: IFile
        get() = absoluteDirectoryInternal

    override fun addProjectListener(listener: ProjectListener) {
        loaderListeners.add(listener)
    }

    override fun removeLoadersListener(listener: ProjectListener) {
        loaderListeners.remove(listener)
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        if (component is Loader) {
            val res = getLoaderOrNull(component.file?.path ?: "", component.componentName)
            if (res == null) {
                loadersInternal.add(component)
            }
        }
    }

    override fun update(delta: Float) {
        if (loadingLoaders.size > 0) {
            loadingResourcesToRemove.clear()

            for (i in loadingLoaders.indices) {
                val loader = loadingLoaders[i]
                if (loader.runOnGLThreadRequest) loader.runOnGLThread()
                loader.updateProgress()
                if (loader.isLoaded) {
                    loadingResourcesToRemove.add(loader)

                    for (j in loaderListeners.indices) {
                        loaderListeners[j].loaderCompleted(loader)
                    }
                }
            }

            for (i in loadingResourcesToRemove.indices) {
                loadingLoaders.remove(loadingResourcesToRemove[i])
            }
            loadingResourcesToRemove.clear()
            loadingResourcesToRemove.trimToSize()
        }
    }

    override fun <T : ILoader> load(path: String, loader: T, block: T.() -> Unit): T {
        if (getLoaderOrNull(path, loader.componentName) == null) {
            loader.file = if (AID == this) FS.internal(path) else projectFile(path)
        }

        if (loader.isLoaded || loader.isLoading) {
            block(loader)
        } else {
            block(loader)
            loader.load()
        }

        return loader
    }

    override fun load(path: String, loaderName: String, block: ILoader.() -> Unit): ILoader {
        if (loaderName.isEmpty()) {
            throw IllegalArgumentException("Project: loader name is empty")
        }

        var loader: ILoader? = getLoaderOrNull(path, loaderName)
        if (loader == null) {
            loader = entity.makePath(path).componentTyped<ILoader>(loaderName)
            loader.file = if (AID == this) FS.internal(path) else projectFile(path)
        }

        if (loader.isLoaded || loader.isLoading) {
            block(loader)
        } else {
            block(loader)
            loader.load()
        }

        return loader
    }

    override fun monitorLoading(loader: ILoader) {
        if (!loadingLoaders.contains(loader)) loadingLoaders.add(loader)

        for (j in loaderListeners.indices) {
            loaderListeners[j].loaderStarted(loader)
        }
    }

    override fun getLoaderOrNull(uri: String, loaderName: String): ILoader? {
        for (i in loadersInternal.indices) {
            val loader = loadersInternal[i]
            if (loader.componentName == loaderName && loader.file?.path == uri) {
                return loader
            }
        }

        return null
    }

    override fun destroy() {
        super.destroy()
        loaderListeners.clear()
        entity.children.forEach { it.destroy() }
    }
}

val RES: IProject = Project().apply { getOrCreateEntity().name = "Project" }

/** AID can be used for storing auxiliary resources */
val AID: IProject = Project().apply { getOrCreateEntity().name = "Aid" }

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ILoader> IProject.load(uri: String, noinline block: T.() -> Unit = {}): T =
    load(uri, T::class.simpleName!!, block as ILoader.() -> Unit) as T

fun openThelemaProject(file: IFile) {
    val file2 = if (file.isDirectory) file.child(PROJECT_FILENAME) else file
    file2.readText {
        try {
            val json = JSON.parseObject(it)
            RES.file = file2
            RES.entity.destroy()
            RES.getOrCreateEntity().readJson(json)
            RES.entity.name = "Project"
        } catch (ex: Exception) {
            LOG.error("Error while loading project ${file.path}")
            ex.printStackTrace()
        }
    }
}

const val PROJECT_FILENAME = "project.thelema"