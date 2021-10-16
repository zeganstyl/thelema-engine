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

import app.thelema.concurrency.AtomRef
import java.util.concurrent.atomic.AtomicReference

class AtomRefJvm<T>(initial: T?): AtomRef<T> {
    private val wrap = AtomicReference<T>(initial)

    override var value: T?
        get() = wrap.get()
        set(value) { wrap.set(value) }

    override fun lazySet(newValue: T?) = wrap.lazySet(newValue)
    override fun compareAndSet(expect: T?, newValue: T?): Boolean = wrap.compareAndSet(expect, newValue)
    override fun getAndSet(newValue: T?): T? = wrap.getAndSet(newValue)
}