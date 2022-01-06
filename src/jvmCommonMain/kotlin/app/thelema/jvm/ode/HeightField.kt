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

package app.thelema.jvm.ode

import app.thelema.phys.IHeightField
import app.thelema.phys.HeightProvider
import org.ode4j.ode.DHeightfield
import org.ode4j.ode.DMass
import org.ode4j.ode.OdeHelper

/** http://ode.org/wiki/index.php?title=Manual#Heightfield_Class
 *
 * @author zeganstyl */
class HeightField: OdeShapeAdapter<DHeightfield>(), IHeightField {
    var width: Float = 1f
    var depth: Float = 1f
    var widthSamples: Int = 1
    var depthSamples: Int = 1
    var scale: Float = 1f
    var offset: Float = 0f
    var thickness: Float = 0f
    var tiling: Boolean = false

    override var heightProvider: HeightProvider? = null
    private val heightCallback = DHeightfield.DHeightfieldGetHeight { userData, x, y ->
        heightProvider?.provideHeight(userData, x, y)?.toDouble() ?: 0.0
    }

    val data = OdeHelper.createHeightfieldData()

    override fun createGeom(): DHeightfield {
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

        return OdeHelper.createHeightfield(null, data, true)
    }

    override fun setupMass(density: Double, mass: DMass) {}

    override fun destroyGeom() {
        super.destroyGeom()
        data.destroy()
    }
}