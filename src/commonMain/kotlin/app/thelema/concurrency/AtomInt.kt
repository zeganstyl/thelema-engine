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

package app.thelema.concurrency

interface AtomInt {
    var value: Int

    fun lazySet(newValue: Int)

    fun compareAndSet(expect: Int, newValue: Int): Boolean

    fun getAndSet(newValue: Int): Int

    fun getAndIncrement(): Int

    fun getAndDecrement(): Int

    fun getAndAdd(delta: Int): Int

    fun addAndGet(delta: Int): Int

    fun incrementAndGet(): Int

    fun decrementAndGet(): Int

    operator fun plusAssign(delta: Int)

    operator fun minusAssign(delta: Int)
}