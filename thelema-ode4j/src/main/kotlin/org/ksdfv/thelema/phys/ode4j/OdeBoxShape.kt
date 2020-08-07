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

import org.ksdfv.thelema.phys.IBoxShape
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeBoxShape(
    world: OdePhysicsWorld,
    xSize: Float,
    ySize: Float,
    zSize: Float
): IBoxShape, OdeGeom() {
    val box = OdeHelper.createBox(world.space, xSize.toDouble(), ySize.toDouble(), zSize.toDouble())

    override val geom
        get() = box

    override var xSize: Float
        get() = box.lengths.get0().toFloat()
        set(value) {
            box.setLengths(value.toDouble(), ySize.toDouble(), zSize.toDouble())
        }

    override var ySize: Float
        get() = box.lengths.get1().toFloat()
        set(value) {
            box.setLengths(xSize.toDouble(), value.toDouble(), zSize.toDouble())
        }

    override var zSize: Float
        get() = box.lengths.get2().toFloat()
        set(value) {
            box.setLengths(xSize.toDouble(), ySize.toDouble(), value.toDouble())
        }

    override var friction: Float = 1f

    override var userObject: Any = this

    override val shapeType: Int
        get() = IShape.BoxType

    init {
        geom.data = this
    }
}