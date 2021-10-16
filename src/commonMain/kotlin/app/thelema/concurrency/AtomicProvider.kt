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

import app.thelema.concurrency.stub.AtomicProviderStub

interface AtomicProvider {
    fun int(initial: Int = 0): AtomInt
    fun bool(initial: Boolean = false): AtomBool
    fun <T> ref(initial: T? = null): AtomRef<T>
    fun <E> list(): ConcurrentList<E>
    fun <K, V> map(): ConcurrentMap<K, V>
    fun <E> set(): ConcurrentSet<E>
}

var ATOM: AtomicProvider = AtomicProviderStub()