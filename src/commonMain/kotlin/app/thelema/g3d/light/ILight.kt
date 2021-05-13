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

package app.thelema.g3d.light

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IScene
import app.thelema.g3d.node.ITransformNode
import app.thelema.img.ITexture
import app.thelema.math.IMat4
import app.thelema.math.IVec3

/** @author zeganstyl */
interface ILight: IEntityComponent {
    val node: ITransformNode

    val color: IVec3

    var lightIntensity: Float

    /** Use [LightType] */
    val lightType: Int

    var isLightEnabled: Boolean

    var isShadowEnabled: Boolean

    val shadowMaps: Array<ITexture>

    /** View projection matrix for each shadow map */
    val viewProjectionMatrices: Array<IMat4>

    fun setupShadowMaps(width: Int = 1024, height: Int = 1024)

    fun renderShadowMaps(scene: IScene)

    fun set(other: ILight): ILight {
        color.set(other.color)
        lightIntensity = other.lightIntensity
        return this
    }
}