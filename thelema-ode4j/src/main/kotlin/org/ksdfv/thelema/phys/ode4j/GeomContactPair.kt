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

package org.ksdfv.thelema.phys.ode4j

// https://stackoverflow.com/questions/24262897/integer-pair-add-to-hashset-java

/** Used to handle collisions
 *
 * @author zeganstyl */
class GeomContactPair {
    lateinit var a: IOdeGeom
    lateinit var b: IOdeGeom

    var depth: Double = 0.0

    var lifeTime: Float = 0f

    override fun equals(other: Any?): Boolean {
        other as GeomContactPair
        return other.a == a && other.b == b
    }

    override fun hashCode(): Int = a.hashCode() * 31 + b.hashCode()
}