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
import app.thelema.ecs.component
import app.thelema.fs.IFile

interface ILoader: IEntityComponent {
    var file: IFile?

    val progress: Float
        get() = if (maxProgress > 0) currentProgress.toFloat() / maxProgress else 0f

    val currentProgress: Int
    val maxProgress: Int

    val isLoaded: Boolean

    val isLoading: Boolean

    val holders: List<Any>

    var runOnGLThreadRequest: Boolean

    var separateThread: Boolean?

    fun loadDependency(uri: String, loaderName: String): ILoader =
        entity.getRootEntity().component<IProject>().load(uri, loaderName)

    fun loadDependencyRelative(path: String, loaderName: String): ILoader =
        entity.getRootEntity().component<IProject>().load(file!!.parent().path + '/' + path, loaderName)

    fun <T: ILoader> loadDependencyRelative(path: String, loader: T): T =
        entity.getRootEntity().component<IProject>().load(file!!.parent().path + '/' + path, loader)

    fun initProgress()

    fun updateProgress()

    /** Some resources needs to call OpenGL functions. These functions can be run only on OpenGL thread.
     * If [runOnGLThreadRequest] is true, [RES] will call this function on OpenGL thread. */
    fun runOnGLThread() {}

    fun hold(holder: Any)

    fun release(holder: Any)

    fun addLoaderListener(listener: LoaderListener)

    fun removeLoaderListener(listener: LoaderListener)

    /** If resource not loaded yet, listener will be added, and then removed after loading.
     * If resource already loaded, [ready] will be called directly.
     *
     * @param notifyOnce if true, listener will be removed after notify */
    fun onLoaded(notifyOnce: Boolean = true, error: (status: Int) -> Unit = {}, ready: () -> Unit): LoaderListener? {
        if (isLoaded) {
            ready()
        } else {
            val listener = object : LoaderListener {
                override val removeListenerOnLoaded: Boolean = notifyOnce

                override fun loaded(loader: ILoader) {
                    ready()
                }

                override fun error(resource: ILoader, status: Int) {
                    error(status)
                }
            }
            addLoaderListener(listener)
            return listener
        }
        return null
    }

    /** Forced loading on current thread */
    fun loadBase(file: IFile)

    fun load()

    fun reload()

    fun stop(status: Int = 200)
}

inline fun <reified T: ILoader> ILoader.loadDependency(uri: String): ILoader =
    loadDependency(uri, T::class.simpleName!!)

@Suppress("UNCHECKED_CAST")
inline fun <reified T: ILoader> ILoader.loadDependency(uri: String, noinline block: T.() -> Unit = {}): T =
    loadDependency(uri, T::class.simpleName!!).apply(block as ILoader.() -> Unit) as T

@Suppress("UNCHECKED_CAST")
inline fun <reified T: ILoader> ILoader.loadDependencyRelative(path: String, noinline block: T.() -> Unit = {}): T =
    loadDependencyRelative(path, T::class.simpleName!!).apply(block as ILoader.() -> Unit) as T
