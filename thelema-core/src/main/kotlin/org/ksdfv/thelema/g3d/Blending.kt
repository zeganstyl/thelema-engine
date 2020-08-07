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

package org.ksdfv.thelema.g3d

/** @author zeganstyl */
object Blending {
    const val Opaque = 0
    const val Clip = 1
    const val Blend = 2
    const val Hashed = 3

    val items: MutableList<Int> = ArrayList()

    init {
        items.add(Opaque)
        items.add(Clip)
        items.add(Blend)
        items.add(Hashed)
    }

    fun getByName(name: String): Int = when (name) {
        "Opaque" -> Opaque
        "Clip" -> Clip
        "Blend" -> Blend
        "Hashed" -> Hashed
        else -> throw IllegalStateException("No blending with name \"$name\"")
    }

    fun getById(id: Int): String = when (id) {
        Opaque -> "Opaque"
        Clip -> "Clip"
        Blend -> "Blend"
        Hashed -> "Hashed"
        else -> throw IllegalStateException("No blending with id = $id")
    }
}