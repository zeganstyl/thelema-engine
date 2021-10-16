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

import app.thelema.concurrency.ATOM
import app.thelema.ecs.*
import app.thelema.fs.IFile

abstract class LoaderAdapter: ILoader {
    override var file: IFile?
        get() = loader.file
        set(value) { loader.file = value }

    protected val currentProgressInternal = ATOM.int(0)
    override var currentProgress: Int
        get() = currentProgressInternal.value
        set(value) { currentProgressInternal.value = value }

    protected val maxProgressInternal = ATOM.int(0)
    override var maxProgress: Int
        get() = maxProgressInternal.value
        set(value) { maxProgressInternal.value = value }

    override val holders: List<Any>
        get() = loader.holders

    override var separateThread: Boolean?
        get() = loader.separateThread
        set(value) { loader.separateThread = value }

    override var runOnGLThreadRequest: Boolean
        get() = loader.runOnGLThreadRequest
        set(value) { loader.runOnGLThreadRequest = value }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            loader = value?.component() ?: Loader()
            loader.proxy = this
        }

    override val isLoaded: Boolean
        get() = loader.isLoaded

    override val isLoading: Boolean
        get() = loader.isLoading

    var loader = Loader()

    override fun stop(status: Int) {
        loader.stop(status)
    }

    override fun load() {
        loader.load()
    }

    override fun reload() {
        loader.reload()
    }

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    override fun hold(holder: Any) {
        loader.hold(holder)
    }

    override fun release(holder: Any) {
        loader.release(holder)
    }

    override fun updateProgress() {}

    override fun addLoaderListener(listener: LoaderListener) {
        loader.addLoaderListener(listener)
    }

    override fun removeLoaderListener(listener: LoaderListener) {
        loader.removeLoaderListener(listener)
    }
}
