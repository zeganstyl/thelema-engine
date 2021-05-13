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

import app.thelema.ecs.IEntity
import app.thelema.phys.IBoxShape
import app.thelema.phys.IShape
import org.ode4j.ode.DBox
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class BoxShape: IBoxShape {
    var box: DBox? = null

    /** Space names */
    private val spaces = ArrayList<String>()

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = box
        }

    override var shape: IShape = Shape().also { it.geom = box }

    override var xSize: Float
        get() = box?.lengths?.get0()?.toFloat() ?: 0f
        set(value) {
            box?.setLengths(value.toDouble(), ySize.toDouble(), zSize.toDouble())
        }

    override var ySize: Float
        get() = box?.lengths?.get1()?.toFloat() ?: 0f
        set(value) {
            box?.setLengths(xSize.toDouble(), value.toDouble(), zSize.toDouble())
        }

    override var zSize: Float
        get() = box?.lengths?.get2()?.toFloat() ?: 0f
        set(value) {
            box?.setLengths(xSize.toDouble(), ySize.toDouble(), value.toDouble())
        }

    override fun startSimulation() {
        box = OdeHelper.createBox(xSize.toDouble(), ySize.toDouble(), zSize.toDouble()).also { it.data = this }
    }

    override fun endSimulation() {
        box?.destroy()
        box = null
    }
}