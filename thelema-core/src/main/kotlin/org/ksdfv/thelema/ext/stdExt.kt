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

package org.ksdfv.thelema.ext

import java.util.*
import kotlin.collections.ArrayList

// <List> ==============================================================================================================
/** Safe traverse will check if element is null */
inline fun <T> List<T>.traverseSafe(block: (item: T) -> Unit) {
    var i = 0
    while (i < size) {
        val item = get(i)
        if (item != null) block(item)
        i++
    }
}

inline fun <T> List<T>.contains(predicate: (item: T) -> Boolean): Boolean {
    var i = 0
    while (i < size) {
        val item = get(i)
        if (item != null) {
            if (predicate(item)) return true
        }

        i++
    }

    return false
}

fun <T> List<T>.leftShift(d: Int): ArrayList<T> {
    val newList = ArrayList<T>(this)
    var shift = d
    if (shift > size) shift %= size
    forEachIndexed { index, value ->
        val newIndex = (index + (size - shift)) % size
        newList[newIndex] = value
    }
    return newList
}

fun <T> List<T>.swap(index1: Int, index2: Int) = Collections.swap(this, index1, index2)
// </List> =============================================================================================================

fun <T> MutableCollection<T>.add(vararg items: T): MutableCollection<T> {
    for (i in items.indices) {
        add(items[i])
    }

    return this
}

// <Primitive array> ===================================================================================================
/** Safe traverse will check if element is null */
fun <T> Array<T>.traverseSafe(block: (item: T) -> Unit) {
    var i = 0
    while (i < size) {
        val item = get(i)
        if (item != null) block(item)

        i++
    }
}

fun <T> Array<T>.contains(predicate: (item: T) -> Boolean): Boolean {
    var i = 0
    while (i < size) {
        val item = get(i)
        if (item != null) {
            if (predicate(item)) return true
        }

        i++
    }

    return false
}
// </Primitive array> ==================================================================================================