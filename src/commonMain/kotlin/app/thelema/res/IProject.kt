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

import app.thelema.ecs.IEntityComponent
import app.thelema.fs.FS
import app.thelema.fs.IFile

interface IProject: IEntityComponent {
    val providers: List<IResourceProvider>

    val resources: List<Resource>

    var loadOnSeparateThreadByDefault: Boolean

    var file: IFile

    fun addLoadersListener(listener: LoadersListener)

    fun removeLoadersListener(listener: LoadersListener)

    fun addReferenceListener(listener: ReferenceListener)

    fun removeReferenceListener(listener: ReferenceListener)

    fun onComponentAdded(
        entityPath: String,
        componentName: String,
        isDisposable: Boolean = true,
        block: (component: IEntityComponent) -> Unit
    ): ReferenceListener?

    fun resourceUriChanged(oldValue: String, newValue: String)

    /** Must be called on OpenGL thread */
    fun update(delta: Float)

    fun addLoaderProvider(provider: IResourceProvider)

    fun removeResourceType(provider: IResourceProvider)

    fun canLoad(uri: String): Boolean =
        providers.firstOrNull { it.canLoad(FS.internal(uri)) } != null

    fun canLoad(file: IFile): Boolean =
        providers.firstOrNull { it.canLoad(file) } != null

    fun load(uri: String, loader: String = "", block: ILoader.() -> Unit = {}): ILoader =
        load(FS.internal(uri), loader, block)

    fun load(file: IFile, loader: String = "", block: ILoader.() -> Unit = {}): ILoader

    fun <T : ILoader> loadTyped(uri: String, loader: String = "", block: T.() -> Unit = {}): T =
        load(uri, loader, block as (ILoader.() -> Unit)) as T

    fun <T : ILoader> loadTyped(file: IFile, loader: String = "", block: T.() -> Unit = {}): T =
        load(file, loader, block as (ILoader.() -> Unit)) as T

    operator fun get(uri: String): ILoader = getOrNull(uri)!!

    fun getOrNull(uri: String): ILoader?

    fun <T: ILoader> getTyped(uri: String): T = get(uri) as T

    fun <T: ILoader> getTypedOrNull(uri: String): T? = getOrNull(uri) as T?

    fun remove(uri: String)
}

inline fun <reified T: IEntityComponent> IProject.onComponentAdded(
    entityPath: String,
    isDisposable: Boolean = true,
    noinline block: (component: T) -> Unit
): ReferenceListener? = RES.project.onComponentAdded(entityPath, T::class.simpleName!!, isDisposable, block as (component: IEntityComponent) -> Unit)