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
import app.thelema.ecs.IEntityComponent
import app.thelema.fs.IFile

interface IProject: IEntityComponent {
    val loaders: List<ILoader>

    var loadOnSeparateThreadByDefault: Boolean

    /** Project file */
    var file: IFile

    /** Project directory */
    val absoluteDirectory: IFile

    var mainScene: EntityLoader?

    fun runMainScene()

    fun addProjectListener(listener: ProjectListener)

    fun removeLoadersListener(listener: ProjectListener)

    fun monitorLoading(loader: ILoader)

    /** Must be called on OpenGL thread */
    fun update(delta: Float)

    fun load(path: String, loaderName: String, block: ILoader.() -> Unit = {}): ILoader

    fun <T: ILoader> load(path: String, loader: T, block: T.() -> Unit = {}): T

    fun getLoaderOrNull(uri: String, loaderName: String): ILoader?
}