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

import org.ksdfv.thelema.phys.IHeightField
import org.ksdfv.thelema.phys.IHeightProvider
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.DHeightfield
import org.ode4j.ode.OdeHelper

/** http://ode.org/wiki/index.php?title=Manual#Heightfield_Class
 * @author zeganstyl */
class OdeHeightField(
    world: OdePhysicsWorld,
    var width: Float,
    var depth: Float,
    var widthSamples: Int,
    var depthSamples: Int,
    var scale: Float = 1f,
    var offset: Float = 0f,
    var thickness: Float = 0f,
    var tiling: Boolean = false,
    override var heightProvider: IHeightProvider? = null
): IHeightField, OdeGeom() {
    val heightField: DHeightfield

    override val geom: DGeom
        get() = heightField

    override val shapeType: Int
        get() = IShape.HeightField

    private val heightCallback = DHeightfield.DHeightfieldGetHeight { userData, x, y ->
        heightProvider?.provideHeight(userData, x, y)?.toDouble() ?: 0.0
    }

    val data = OdeHelper.createHeightfieldData()

    init {
        data.buildCallback(
            this,
            heightCallback,
            width.toDouble(),
            depth.toDouble(),
            widthSamples,
            depthSamples,
            scale.toDouble(),
            offset.toDouble(),
            thickness.toDouble(),
            tiling
        )

        heightField = OdeHelper.createHeightfield(world.space, data, true)
        geom.data = this
    }

    override fun destroy() {
        heightProvider = null
        data.destroy()
        super.destroy()
    }
}