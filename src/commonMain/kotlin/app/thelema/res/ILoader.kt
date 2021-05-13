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

interface ILoader: IEntityComponent {
    val uri: String

    val progress: Float
        get() = currentProgress.toFloat() / maxProgress

    var currentProgress: Long
    var maxProgress: Long

    val isLoaded: Boolean
        get() = currentProgress == maxProgress

    val isLoading: Boolean
        get() = currentProgress > 0 && !isLoaded

    val holders: List<Any>

    val runOnGLThreadRequest: Boolean

    var loadOnSeparateThread: Boolean

    fun initProgress()

    fun updateProgress()

    /** Some resources needs to call OpenGL functions. These functions can be run only on OpenGL thread.
     * If [runOnGLThreadRequest] is true, [RES] will call this function on OpenGL thread. */
    fun runOnGLThread() {}

    fun hold(holder: Any)

    fun release(holder: Any)

    fun addResourceListener(listener: ResourceListener)

    fun removeResourceListener(listener: ResourceListener)

    /** If resource not loaded yet, listener will be added, and then removed after loading.
     * If resource already loaded, [ready] will be called directly.
     *
     * @param notifyOnce if true, listener will be removed after notify */
    fun onLoaded(notifyOnce: Boolean = true, error: (status: Int) -> Unit = {}, ready: () -> Unit): ResourceListener? {
        if (isLoaded) {
            ready()
        } else {
            val listener = object : ResourceListenerAdapter() {
                override fun removeListenerOnLoaded(): Boolean = notifyOnce

                override fun loaded(resource: ILoader) {
                    ready()
                }

                override fun error(resource: ILoader, status: Int) {
                    error(status)
                }
            }
            addResourceListener(listener)
            return listener
        }
        return null
    }

    fun load()
}
