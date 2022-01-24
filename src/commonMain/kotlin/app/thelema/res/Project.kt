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

import app.thelema.ecs.ECS
import app.thelema.ecs.EntityLoader
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.fs.*
import app.thelema.g3d.scene
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

    private val _loaders = ArrayList<ILoader>()
    override val loaders: List<ILoader>
        get() = _loaders

    private val loadingLoaders = ArrayList<ILoader>()
    private val loadingResourcesToRemove = ArrayList<ILoader>()

    override var loadOnSeparateThreadByDefault: Boolean = false

    private val loadersListeners = ArrayList<LoadersListener>()

    override var file: IFile = DefaultProjectFile
        set(value) {
            field = value
            absoluteDirectoryInternal = value.parent()
        }

    override var mainScene: EntityLoader? = null
        set(value) {
            if (field != value) {
                field = value
                if (runMainSceneRequest) mainScene?.load()
            }
        }

    private var absoluteDirectoryInternal: IFile = DefaultProjectDirectory
    override val absoluteDirectory: IFile
        get() = absoluteDirectoryInternal

    private var runMainSceneRequest = false

    override var appPackage: String = ""

    override fun runMainScene() {
        runMainSceneRequest = true
        mainScene?.load()
    }

    override fun addLoadersListener(listener: LoadersListener) {
        loadersListeners.add(listener)
    }

    override fun removeLoadersListener(listener: LoadersListener) {
        loadersListeners.remove(listener)
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        if (component is Loader) {
            val res = getLoaderOrNull(component.file?.path ?: "", component.componentName)
            if (res == null) {
                _loaders.add(component)
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

                    for (j in loadersListeners.indices) {
                        loadersListeners[j].loaderCompleted(loader)
                    }
                }
            }

            for (i in loadingResourcesToRemove.indices) {
                loadingLoaders.remove(loadingResourcesToRemove[i])
            }
            loadingResourcesToRemove.clear()
            loadingResourcesToRemove.trimToSize()
        } else {
            if (runMainSceneRequest) {
                runMainSceneRequest = false
                mainScene?.also {
                    it.targetEntity.makeCurrent()
                    it.targetEntity.scene().startSimulation()
                }
            }
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
            val file = if (AID == this) FS.internal(path) else projectFile(path)
            loader.file = file
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

        for (j in loadersListeners.indices) {
            loadersListeners[j].loaderStarted(loader)
        }
    }

    override fun getLoaderOrNull(uri: String, loaderName: String): ILoader? {
        for (i in _loaders.indices) {
            val loader = _loaders[i]
            if (loader.componentName == loaderName && loader.file?.path == uri) {
                return loader
            }
        }

        return null
    }

    override fun destroy() {
        super.destroy()
        entityOrNull?.also { entity ->
            entity.forEachChildEntity { it.destroy() }
            entity.clearChildren()
        }
        mainScene = null
        loadersListeners.clear()
        loadingResourcesToRemove.clear()
        loadingLoaders.clear()
        _loaders.clear()
        loadersListeners.trimToSize()
        loadingResourcesToRemove.trimToSize()
        loadingLoaders.trimToSize()
        _loaders.trimToSize()
        entity.forEachChildEntity { it.destroy() }
    }
}

val RES: IProject = Project().apply { getOrCreateEntity().name = "App" }

/** AID can be used for storing auxiliary resources */
val AID: IProject = Project().apply { getOrCreateEntity().name = "Aid" }

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ILoader> IProject.load(uri: String, noinline block: T.() -> Unit = {}): T =
    load(uri, T::class.simpleName!!, block as ILoader.() -> Unit) as T

fun openThelemaApp(file: IFile) {
    val file2 = if (file.isDirectory) file.child(APP_ROOT_FILENAME) else file
    file2.readText {
        try {
            val json = JSON.parseObject(it)
            RES.file = file2
            RES.destroy()
            RES.getOrCreateEntity().readJson(json)
            RES.entity.name = "App"
            ECS.addEntity(RES.entity)
        } catch (ex: Exception) {
            LOG.error("Error while loading project ${file.path}")
            ex.printStackTrace()
        }
    }
}

/** Open root app.thelema in resources */
fun openThelemaApp() = openThelemaApp(FS.internal(APP_ROOT_FILENAME))

/** Open root app.thelema in resources and run main scene */
fun runMainScene() {
    openThelemaApp()
    RES.runMainScene()
}

const val APP_ROOT_FILENAME = "app.thelema"