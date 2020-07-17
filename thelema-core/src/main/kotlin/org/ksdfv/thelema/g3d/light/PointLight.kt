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

import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
class PointLight: ILight {
    override var name: String = ""

    override val lightType: Int
        get() = LightType.Point

    override var isLightEnabled: Boolean = true
    override var lightIntensity = 1f
    override var color: IVec3 = Vec3(1f, 1f, 1f)

    var position: IVec3 = Vec3()
    var range = 100f

    override var isShadowEnabled: Boolean = false

    override fun setupShadowMaps(width: Int, height: Int) {}
    override fun renderShadowMaps(scene: IScene) {}

    override val shadowMaps: Array<ITexture> = DirectionalLight.ShadowMapsCap
    override val viewProjectionMatrices: Array<IMat4> = DirectionalLight.ViewProjectionMatricesCap

    override fun set(other: ILight): PointLight {
        super.set(other)
        if (other.lightType == LightType.Point) {
            other as PointLight
            position.set(other.position)
            range = other.range
        }
        return this
    }

    override fun copy(): PointLight = PointLight().set(this)
}