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
import app.thelema.fs.FS
import app.thelema.fs.IFile
import app.thelema.json.JSON
import app.thelema.utils.LOG

object RES: IProject {
    override var entityOrNull: IEntity?
        get() = project.entityOrNull
        set(value) {
            project.entityOrNull = value
        }

    override val componentName: String
        get() = project.componentName

    val project: IProject = Project().apply { getOrCreateEntity().name = "Project" }

    override val providers: List<IResourceProvider>
        get() = project.providers

    override val resources: List<Resource>
        get() = project.resources

    override var loadOnSeparateThreadByDefault: Boolean
        get() = project.loadOnSeparateThreadByDefault
        set(value) {
            project.loadOnSeparateThreadByDefault = value
        }

    override var file: IFile
        get() = project.file
        set(value) {
            project.file = value
        }

    override fun addLoadersListener(listener: LoadersListener) = project.addLoadersListener(listener)

    override fun removeLoadersListener(listener: LoadersListener) = project.removeLoadersListener(listener)

    override fun addReferenceListener(listener: ReferenceListener) = project.addReferenceListener(listener)

    override fun removeReferenceListener(listener: ReferenceListener) = project.removeReferenceListener(listener)

    override fun onComponentAdded(
        entityPath: String,
        componentName: String,
        isDisposable: Boolean,
        block: (component: IEntityComponent) -> Unit
    ): ReferenceListener? = project.onComponentAdded(entityPath, componentName, isDisposable, block)

    override fun resourceUriChanged(oldValue: String, newValue: String) =
        project.resourceUriChanged(oldValue, newValue)

    fun openProject(file: IFile) {
        val file2 = if (file.isDirectory) file.child("project.thelema") else file
        file2.readText {
            try {
                val json = JSON.parseObject(it)
                project.file = file2
                project.getOrCreateEntity().readJson(json)
                project.entity.name = "Project"
            } catch (ex: Exception) {
                LOG.error("Error while loading project ${file.path}")
                ex.printStackTrace()
            }
        }
    }

    /** Open project from internal root directory */
    fun openProject() {
        openProject(FS.internal("project.thelema"))
    }

    override fun addLoaderProvider(provider: IResourceProvider) = project.addLoaderProvider(provider)

    override fun removeResourceType(provider: IResourceProvider) = project.removeResourceType(provider)

    override fun canLoad(file: IFile): Boolean = project.canLoad(file)

    override fun load(file: IFile, loader: String, block: ILoader.() -> Unit): ILoader =
        project.load(file, loader, block)

    override fun <T : ILoader> loadTyped(file: IFile, loader: String, block: T.() -> Unit): T =
        project.loadTyped(file, loader, block)

    override fun load(uri: String, loader: String, block: ILoader.() -> Unit): ILoader =
        project.load(uri, loader, block)

    override fun canLoad(uri: String): Boolean = project.canLoad(uri)

    override fun <T : ILoader> loadTyped(uri: String, loader: String, block: T.() -> Unit): T =
        project.loadTyped(uri, loader, block)

    override fun update(delta: Float) = project.update(delta)

    override fun get(uri: String): ILoader = project[uri]

    override fun getOrNull(uri: String): ILoader? = project.getOrNull(uri)

    override fun <T : ILoader> getTyped(uri: String): T = project.getTyped(uri)

    override fun <T : ILoader> getTypedOrNull(uri: String): T? = project.getTypedOrNull(uri)

    override fun remove(uri: String) = project.remove(uri)

    override fun destroy() = project.destroy()
}