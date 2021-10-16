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

package app.thelema.concurrency.stub

import app.thelema.concurrency.AtomInt

class AtomIntStub(override var value: Int): AtomInt {
    override fun lazySet(newValue: Int) {
        this.value = newValue
    }

    override fun compareAndSet(expect: Int, newValue: Int): Boolean {
        if (value == expect) {
            value = newValue
            return true
        }
        return false
    }

    override fun getAndSet(newValue: Int): Int {
        val result = this.value
        this.value = newValue
        return result
    }

    override fun getAndIncrement(): Int {
        val result = value
        value++
        return result
    }

    override fun getAndDecrement(): Int {
        val result = value
        value--
        return result
    }

    override fun getAndAdd(delta: Int): Int {
        val result = value
        value += delta
        return result
    }

    override fun addAndGet(delta: Int): Int {
        value += delta
        return value
    }

    override fun incrementAndGet(): Int {
        value++
        return value
    }

    override fun decrementAndGet(): Int {
        value--
        return value
    }

    override fun plusAssign(delta: Int) {
        value += delta
    }

    override fun minusAssign(delta: Int) {
        value -= delta
    }
}