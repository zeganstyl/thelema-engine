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

package org.ksdfv.thelema.g3d.light

import org.ksdfv.thelema.math.IVec3

/** @author zeganstyl */
interface ILight {
    var name: String

    var color: IVec3

    var intensity: Float

    /** Use [LightType] */
    val lightType: Int

    var isEnabled: Boolean

    fun set(other: ILight): ILight {
        color.set(other.color)
        intensity = other.intensity
        return this
    }

    fun copy(): ILight
}