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

package app.thelema.utils

/** @author zeganstyl */
class Pool<T>(val create: () -> T, val reset: ((instance: T) -> Unit)?) {
    constructor(create: () -> T): this(create, null)

    val free = ArrayList<T>()
    val used = ArrayList<T>()

    fun get(): T {
        val obj = if (free.isNotEmpty()) {
            val instance = free[0]
            free.remove(instance)
            instance
        } else {
            create()
        }
        reset?.invoke(obj)
        used.add(obj)
        return obj
    }

    inline fun getOrCreate(create: () -> T): T {
        val obj = if (free.isNotEmpty()) {
            val instance = free[0]
            free.remove(instance)
            instance
        } else {
            create()
        }
        reset?.invoke(obj)
        used.add(obj)
        return obj
    }

    fun free(instance: T) {
        free.add(instance)
        used.remove(instance)
    }

    fun free(instances: List<T>) {
        free.addAll(instances)
        used.removeAll(instances)
    }

    fun clear() {
        free.clear()
        used.clear()
    }
}