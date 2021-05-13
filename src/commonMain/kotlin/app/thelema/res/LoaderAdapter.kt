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
import app.thelema.ecs.component

abstract class LoaderAdapter: ILoader {
    protected val listeners = ArrayList<ResourceListener>()

    protected var uriInternal: String = ""
    override val uri: String
        get() = uriInternal

    override var currentProgress: Long = 0
    override var maxProgress: Long = 1

    protected var holdersInternal = ArrayList<Any>()
    override val holders: List<Any>
        get() = holdersInternal

    override var loadOnSeparateThread: Boolean = false

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.component<Resource>()?.loaderOrNull = this
        }

    fun resource(): Resource = entity.component()

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    protected open fun notifyLoaded() {
        notifyAndCleanListeners { it.loaded(this) }
    }

    private fun notifyAndCleanListeners(block: (listener: ResourceListener) -> Unit) {
        val toRemove = ArrayList<ResourceListener>()

        for (i in listeners.indices) {
            val listener = listeners[i]
            block(listener)
            if (listener.removeListenerOnLoaded()) toRemove.add(listener)
        }

        for (i in toRemove.indices) {
            listeners.remove(toRemove[i])
        }
    }

    protected open fun notifyError(status: Int) {
        notifyAndCleanListeners { it.error(this, status) }
    }

    override fun hold(holder: Any) {
        holdersInternal.add(holder)
    }

    override fun release(holder: Any) {
        holdersInternal.remove(holder)
    }

    override fun updateProgress() {}

    override fun addResourceListener(listener: ResourceListener) {
        listeners.add(listener)
    }

    override fun removeResourceListener(listener: ResourceListener) {
        listeners.remove(listener)
    }
}