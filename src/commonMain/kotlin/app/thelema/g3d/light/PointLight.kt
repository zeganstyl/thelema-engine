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

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.IScene
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.img.ITexture
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.math.Vec3

/** @author zeganstyl */
class PointLight: ILight {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override var node: ITransformNode = TransformNode()

    override val lightType: Int
        get() = LightType.Point

    override val componentName: String
        get() = "PointLight"

    override var isLightEnabled: Boolean = true
    override var intensity = 1f
    override var color: IVec3 = Vec3(1f, 1f, 1f)

    override var range = 100f

    override val direction: IVec3 = Vec3()

    override var innerConeCos: Float = 0f
    override var outerConeCos: Float = 0f

    override var isShadowEnabled: Boolean = false

    override fun setupShadowMaps(width: Int, height: Int) {}
    override fun renderShadowMaps(scene: IScene) {}

    override val shadowMaps: Array<ITexture> = emptyArray()
    override val viewProjectionMatrices: Array<IMat4> = emptyArray()

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        set(other as PointLight)
        return this
    }

    fun set(other: PointLight): PointLight {
        set(other as ILight)
        range = other.range
        return this
    }
}