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

package app.thelema.jvm.concurrency

import app.thelema.concurrency.AtomInt
import java.util.concurrent.atomic.AtomicInteger

class AtomIntJvm(initial: Int): AtomInt {
    private val wrap = AtomicInteger(initial)

    override var value: Int
        get() = wrap.get()
        set(value) { wrap.set(value) }

    override fun lazySet(newValue: Int) = wrap.lazySet(newValue)
    override fun compareAndSet(expect: Int, newValue: Int): Boolean = wrap.compareAndSet(expect, newValue)
    override fun getAndSet(newValue: Int): Int = wrap.getAndSet(newValue)
    override fun getAndIncrement(): Int = wrap.getAndIncrement()
    override fun getAndDecrement(): Int = wrap.getAndDecrement()
    override fun getAndAdd(delta: Int): Int = wrap.getAndAdd(delta)
    override fun addAndGet(delta: Int): Int = wrap.addAndGet(delta)
    override fun incrementAndGet(): Int = wrap.incrementAndGet()
    override fun decrementAndGet(): Int = wrap.decrementAndGet()
    override fun plusAssign(delta: Int) { wrap.addAndGet(delta) }
    override fun minusAssign(delta: Int) { wrap.addAndGet(-delta) }
}