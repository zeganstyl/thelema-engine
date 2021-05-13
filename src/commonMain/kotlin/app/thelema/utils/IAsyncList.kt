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
interface IAsyncList<T>: MutableList<T> {
    val ready: MutableSet<Int>
    val requests: MutableList<Pair<Int, () -> Unit>>

    fun ready(index: Int) {
        ready.add(index)

        val tmp = ArrayList<Pair<Int, () -> Unit>>()
        requests.forEach {
            if (it.first == index) {
                it.second()
            } else {
                tmp.add(it)
            }
        }
        requests.clear()
        requests.addAll(tmp)
        tmp.clear()
    }

    fun getOrWait(index: Int, call: (obj: T) -> Unit) {
        if (ready.contains(index)) {
            call(this[index])
        } else {
            requests.add(Pair(index) {
                call(this[index])
            })
        }
    }

    fun <T2> getOrWaitTyped(index: Int, call: (obj: T2) -> Unit) = getOrWait(index, call as (obj: T) -> Unit)
}