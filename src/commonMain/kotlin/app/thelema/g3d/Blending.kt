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

package app.thelema.g3d

/** Alpha blending modes
 *
 * @author zeganstyl */
object Blending {
    const val OPAQUE = "OPAQUE"
    const val MASK = "MASK"
    const val BLEND = "BLEND"
    const val HASHED = "HASHED"

    val items: MutableList<String> = ArrayList()

    init {
        items.add(OPAQUE)
        items.add(MASK)
        items.add(BLEND)
        items.add(HASHED)
    }
}
