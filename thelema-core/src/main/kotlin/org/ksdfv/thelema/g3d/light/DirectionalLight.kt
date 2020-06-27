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
import org.ksdfv.thelema.math.Vec3

/** @author zeganstyl */
class DirectionalLight: ILight {
    override var name: String = ""

    override val lightType: Int
        get() = LightType.Directional

    override var isEnabled: Boolean = true
    override var intensity: Float = 1f
    override var color: IVec3 = Vec3(1f)

    var direction: IVec3 = Vec3(-1f, -1f, -1f).nor()

    override fun set(other: ILight): DirectionalLight {
        super.set(other)
        if (other.lightType == LightType.Directional) {
            other as DirectionalLight
            direction.set(other.direction)
        }
        return this
    }

    override fun copy(): ILight = DirectionalLight().set(this)
}