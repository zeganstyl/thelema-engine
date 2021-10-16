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

import app.thelema.concurrency.ConcurrentList
import app.thelema.concurrency.ConcurrentMap
import app.thelema.concurrency.ConcurrentSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class ConcurrentListJvm<E>(private val wrap: MutableList<E> = CopyOnWriteArrayList()): ConcurrentList<E>, MutableList<E> by wrap

class ConcurrentMapJvm<K, V>(private val wrap: MutableMap<K, V> = ConcurrentHashMap()): ConcurrentMap<K, V>, MutableMap<K, V> by wrap

class ConcurrentSetJvm<E>(private val wrap: MutableSet<E> = CopyOnWriteArraySet()): ConcurrentSet<E>, MutableSet<E> by wrap
