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

import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.fs.FS
import app.thelema.fs.IFile

class Project: IProject {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (value != null) addedEntityToBranch(value)
        }

    override val componentName: String
        get() = "Project"

    override var file: IFile = FS.internal("")

    private val typesInternal = ArrayList<IResourceProvider>()
    override val providers: List<IResourceProvider>
        get() = typesInternal

    private val resourcesMap = HashMap<String, Resource>()

    private val resourcesInternal = ArrayList<Resource>()
    override val resources: List<Resource>
        get() = resourcesInternal

    private val loadingResources = ArrayList<ILoader>()
    private val loadingResourcesToRemove = ArrayList<ILoader>()

    override var loadOnSeparateThreadByDefault: Boolean = false

    val defaultLoaderProvider = DefaultResourceProvider()

    val referenceListeners = ArrayList<ReferenceListener>()
    val removeListenerRequests = ArrayList<ReferenceListener>()

    val loadersListeners = ArrayList<LoadersListener>()

    init {
        addLoaderProvider(defaultLoaderProvider)
    }

    override fun addLoadersListener(listener: LoadersListener) {
        loadersListeners.add(listener)
    }

    override fun removeLoadersListener(listener: LoadersListener) {
        loadersListeners.remove(listener)
    }

    override fun onComponentAdded(
        entityPath: String,
        componentName: String,
        isDisposable: Boolean,
        block: (component: IEntityComponent) -> Unit
    ): ReferenceListener? {
        val component = entity.getEntityByPath(entityPath)?.getComponentOrNull(componentName)
        if (component != null) {
            block(component)
        } else {
            val listener = object: ReferenceListener {
                override fun componentAdded(component: IEntityComponent) {
                    if (component.entity.path == entityPath && component.isComponentNameAlias(componentName)) {
                        block(component)
                        if (isDisposable) removeListenerRequests.add(this)
                    }
                }
            }
            addReferenceListener(listener)
            return listener
        }
        return null
    }

    override fun addReferenceListener(listener: ReferenceListener) {
        referenceListeners.add(listener)
    }

    override fun removeReferenceListener(listener: ReferenceListener) {
        referenceListeners.remove(listener)
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        for (i in referenceListeners.indices) {
            referenceListeners[i].componentAdded(component)
        }

        if (removeListenerRequests.size > 0) {
            for (i in removeListenerRequests.indices) {
                referenceListeners.remove(removeListenerRequests[i])
            }
            removeListenerRequests.clear()
        }

        if (component is Resource) {
            val res = getOrNull(component.uri)
            if (res == null) {
                resourcesInternal.add(component)
                resourcesMap[component.uri] = component
            }
        }
    }

    override fun resourceUriChanged(oldValue: String, newValue: String) {
        val resource = resourcesMap[oldValue]
        if (resource != null) {
            resourcesMap[newValue] = resource
            resourcesMap.remove(oldValue)
        }
    }

    override fun update(delta: Float) {
        if (loadingResources.size > 0) {
            loadingResourcesToRemove.clear()

            for (i in loadingResources.indices) {
                val loader = loadingResources[i]
                loader.updateProgress()
                if (loader.isLoaded) {
                    if (loader.runOnGLThreadRequest) loader.runOnGLThread()
                    loadingResourcesToRemove.add(loader)

                    for (j in loadersListeners.indices) {
                        loadersListeners[j].loaderCompleted(loader)
                    }
                }
            }

            for (i in loadingResourcesToRemove.indices) {
                loadingResources.remove(loadingResourcesToRemove[i])
            }
            loadingResourcesToRemove.clear()
        }
    }

    override fun addLoaderProvider(provider: IResourceProvider) {
        typesInternal.add(provider)
    }

    override fun removeResourceType(provider: IResourceProvider) {
        typesInternal.remove(provider)
    }

    override fun load(file: IFile, loader: String, block: ILoader.() -> Unit): ILoader {
        var loaderName = loader

        if (loaderName.isEmpty()) {
            for (i in 0 until typesInternal.size) {
                val type = typesInternal[i]
                if (type.canLoad(file)) {
                    loaderName = type.provideLoader(file)
                    break
                }
            }
        }

        if (loaderName.isEmpty()) {
            throw IllegalStateException("Resource loader is not found for: ${file.path}")
        }

        val loaderEntity = entity.mkEntity(file.path)
        val loaderComponent = loaderEntity.componentTyped<ILoader>(loaderName)

        if (loaderComponent.isLoaded || loaderComponent.isLoading) {
            block(loaderComponent)
        } else {
            val resource = loaderEntity.component<Resource>()
            resource.uri = file.path
            resource.file = file
            resourcesMap[file.path] = resource
            resourcesInternal.add(resource)
            loadingResources.add(loaderComponent)
            loaderComponent.loadOnSeparateThread = loadOnSeparateThreadByDefault
            block(loaderComponent)

            if (loaderComponent.loadOnSeparateThread) {
                APP.thread { loaderComponent.load() }
            } else {
                loaderComponent.load()
            }
        }

        for (j in loadersListeners.indices) {
            loadersListeners[j].loaderStarted(loaderComponent)
        }

        return loaderComponent
    }

    override fun getOrNull(uri: String): ILoader? = resourcesMap[uri]?.loaderOrNull

    override fun remove(uri: String) {
        val res = resourcesMap[uri]
        if (res != null) {
            resourcesInternal.remove(res)
            resourcesMap.remove(uri)
        }
    }

    override fun destroy() {
        super.destroy()

        resourcesMap.clear()
        referenceListeners.clear()
        removeListenerRequests.clear()
        loadersListeners.clear()

        entity.children.forEach { it.destroy() }
    }
}
