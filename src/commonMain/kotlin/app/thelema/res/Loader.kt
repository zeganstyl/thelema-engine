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
import app.thelema.concurrency.ATOM
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.fs.IFile
import app.thelema.json.IJsonObject
import app.thelema.net.httpIsSuccess
import app.thelema.utils.LOG

class Loader: ILoader {
    override val componentName: String
        get() = "Loader"

    override var entityOrNull: IEntity? = null

    var proxy: ILoader? = null

    override var file: IFile? = null

    override var runOnGLThreadRequest: Boolean = false

    override var currentProgress: Int
        get() = proxy?.currentProgress ?: 0
        set(_) {}

    override var maxProgress: Int
        get() = proxy?.maxProgress ?: 0
        set(_) {}

    private val holdersInternal = ArrayList<Any>(0)
    override val holders: List<Any>
        get() = holdersInternal

    override var separateThread: Boolean? = null

    private val isLoadedInternal = ATOM.bool(false)
    override val isLoaded: Boolean
        get() = isLoadedInternal.value

    private val isLoadingInternal = ATOM.bool(false)
    override val isLoading: Boolean
        get() = isLoadingInternal.value

    private val listeners = ArrayList<LoaderListener>(0)

    override fun initProgress() {
        proxy?.initProgress()
    }

    override fun updateProgress() {
        proxy?.updateProgress()
    }

    override fun getOrCreateFile(): IFile? = proxy?.getOrCreateFile()

    override fun writeJson(json: IJsonObject) {
        getOrCreateFile()
        super.writeJson(json)
    }

    override fun load() {
        if (!(isLoading || isLoaded)) {
            isLoadingInternal.value = true
            isLoadedInternal.value = false
            runOnGLThreadRequest = false
            initProgress()
            entity.getRootEntity().componentOrNull<IProject>()?.monitorLoading(this)

            file?.also { file ->
                if (file.path.isEmpty()) {
                    LOG.error("$path: file path is empty")
                    stop(404)
                } else if (!file.exists()) {
                    LOG.error("$path: file is not exists")
                    stop(404)
                } else {
                    if (separateThread ?: RES.loadOnSeparateThreadByDefault) {
                        APP.thread {
                            loadBase(file)
                        }
                    } else {
                        loadBase(file)
                    }
                }
            }
        }
    }

    override fun reload() {
        if (isLoaded && !isLoading) {
            file?.also { file ->
                if (file.exists() && file.path.isNotEmpty()) {
                    destroy()
                    isLoadingInternal.value = true
                    isLoadedInternal.value = false
                    runOnGLThreadRequest = false
                    initProgress()
                    entity.getRootEntity().componentOrNull<IProject>()?.monitorLoading(this)

                    if (separateThread ?: RES.loadOnSeparateThreadByDefault) {
                        APP.thread {
                            loadBase(file)
                            isLoadingInternal.value = false
                            isLoadedInternal.value = true
                        }
                    } else {
                        loadBase(file)
                    }
                } else {
                    stop(404)
                }
            }
        }
    }

    override fun hold(holder: Any) {
        holdersInternal.add(holder)
        holdersInternal.trimToSize()
    }

    override fun release(holder: Any) {
        holdersInternal.remove(holder)
        holdersInternal.trimToSize()
    }

    override fun stop(status: Int) {
        isLoadingInternal.value = false
        isLoadedInternal.value = httpIsSuccess(status)

        if (isLoadedInternal.value) {
            notifyAndCleanListeners { it.loaded(this) }
        } else {
            notifyAndCleanListeners { it.error(this, status) }
        }
    }

    private fun notifyAndCleanListeners(block: (listener: LoaderListener) -> Unit) {
        val toRemove = ArrayList<LoaderListener>()

        for (i in listeners.indices) {
            val listener = listeners[i]
            block(listener)
            if (listener.removeListenerOnLoaded) toRemove.add(listener)
        }

        for (i in toRemove.indices) {
            removeLoaderListener(toRemove[i])
        }
    }

    override fun addLoaderListener(listener: LoaderListener) {
        listeners.add(listener)
        listeners.trimToSize()
    }

    override fun removeLoaderListener(listener: LoaderListener) {
        listeners.remove(listener)
        listeners.trimToSize()
    }

    override fun runOnGLThread() {
        proxy?.runOnGLThread()
        runOnGLThreadRequest = false
    }

    override fun loadBase(file: IFile) {
        proxy?.loadBase(file)
    }

    override fun destroy() {
        proxy?.destroy()
    }
}
