/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema

/** @author zeganstyl */
open class Pool<T>(val create: () -> T, val reset: ((instance: T) -> Unit)?) {
    constructor(create: () -> T): this(create, null)

    private val freeInternal = ArrayList<T>()
    val free: List<T>
        get() = freeInternal

    private val usedInternal = ArrayList<T>()
    val used: List<T>
        get() = usedInternal

    fun get(): T {
        val obj = if (free.isNotEmpty()) {
            val instance = free.last()
            freeInternal.remove(instance)
            instance
        } else {
            create()
        }
        reset?.invoke(obj)
        usedInternal.add(obj)
        return obj
    }

    fun free(instance: T) {
        freeInternal.add(instance)
        usedInternal.remove(instance)
    }

    fun free(instances: List<T>) {
        freeInternal.addAll(instances)
        usedInternal.removeAll(instances)
    }

    fun clear() {
        freeInternal.clear()
        usedInternal.clear()
    }
}