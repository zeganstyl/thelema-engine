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

import app.thelema.concurrency.AtomBool
import java.util.concurrent.atomic.AtomicBoolean

class AtomBoolJvm(initial: Boolean): AtomBool {
    private val wrap = AtomicBoolean(initial)

    override var value: Boolean
        get() = wrap.get()
        set(value) { wrap.set(value) }

    override fun lazySet(newValue: Boolean) = wrap.lazySet(newValue)
    override fun compareAndSet(expect: Boolean, newValue: Boolean): Boolean = wrap.compareAndSet(expect, newValue)
    override fun getAndSet(newValue: Boolean): Boolean = wrap.getAndSet(newValue)
}