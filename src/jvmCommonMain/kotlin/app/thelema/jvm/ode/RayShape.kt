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
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNodeListener
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.phys.IRayShape
import app.thelema.phys.IPhysicalShape
import org.ode4j.ode.*


/** @author zeganstyl */
class RayShape: OdeShapeAdapter<DRay>(), IRayShape {
    var contacts = DContactGeomBuffer(1)

    override var length: Float = 1f
        set(value) {
            field = value
            geom?.length = value.toDouble()
        }

    override var position: IVec3 = Vec3()
        set(value) {
            field.set(value)
            geom?.setPosition(value.x.toDouble(), value.y.toDouble(), value.z.toDouble())
        }

    override var direction: IVec3 = Vec3(0f, 0f, 1f)
        set(value) {
            field.set(value)
            updateRay()
        }

    override var directionOffset: IVec3 = Vec3(0f, 0f, 0f)
        set(value) {
            field.set(value)
            updateRay()
        }

    override var useTransformNode: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                node = if (value) entityOrNull?.component() else null
            }
        }

    var node: ITransformNode? = null
        set(value) {
            if (field != value) {
                field?.addListener(nodeListener)
                field = value
                value?.addListener(nodeListener)
            }
        }

    private val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            if (useTransformNode) {
                position.set(node.worldPosition)
                node.worldMatrix.getWorldForward(direction).nor()
                updateRay()
            }
        }
    }

    override var entityOrNull: IEntity?
        get() = super.entityOrNull
        set(value) {
            super.entityOrNull = value
            if (useTransformNode) node = value?.component()
        }

    override fun updateRay() {
        geom?.set(
            position.x.toDouble(),
            position.y.toDouble(),
            position.z.toDouble(),
            (direction.x + directionOffset.x).toDouble(),
            (direction.y + directionOffset.y).toDouble(),
            (direction.z + directionOffset.z).toDouble(),
        )
    }

    override fun checkCollision(shape: IPhysicalShape): Boolean {
        val geom = geom
        return if (geom != null) OdeHelper.collide(geom, shape.sourceObject as DGeom, 1, contacts) != 0 else false
    }

    override fun createGeom(): DRay =
        OdeHelper.createRay(getSpace(), length.toDouble()).also {
            updateRay()
        }

    override fun setRayDirection(x: Float, y: Float, z: Float) {
        direction.set(x, y, z)
        updateRay()
    }

    override fun setupMass(density: Double, mass: DMass) {}
}