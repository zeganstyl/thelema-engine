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

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.math.Vec4
import org.ksdfv.thelema.phys.IShape
import org.ode4j.math.DQuaternion
import org.ode4j.math.DVector3
import org.ode4j.ode.DGeom
import org.ode4j.ode.internal.Rotation

/** @author zeganstyl */
interface IOdeGeom: IShape {
    val geom: DGeom

    override val sourceObject: Any
        get() = geom

    override var position: IVec3
        get() {
            val pos = geom.position
            tmp1.set(pos.get0().toFloat(), pos.get1().toFloat(), pos.get2().toFloat())
            return tmp1
        }
        set(value) {
            geom.setPosition(value.x.toDouble(), value.y.toDouble(), value.z.toDouble())
        }

    override var rotation: IVec4
        get() {
            val pos = geom.quaternion
            tmp2.set(pos.get0().toFloat(), pos.get1().toFloat(), pos.get2().toFloat(), pos.get3().toFloat())
            return tmp2
        }
        set(value) {
            tmp3.set(value.x.toDouble(), value.y.toDouble(), value.z.toDouble(), value.w.toDouble())
            geom.quaternion = tmp3
        }

    override var categoryBits: Long
        get() = geom.categoryBits
        set(value) {
            geom.categoryBits = value
        }

    override var collideBits: Long
        get() = geom.collideBits
        set(value) {
            geom.collideBits = value
        }

    override fun setRotationFromAxis(ax: Float, ay: Float, az: Float, angle: Float): IShape {
        Rotation.dQFromAxisAndAngle(tmp3, ax.toDouble(), ay.toDouble(), az.toDouble(), angle.toDouble())
        geom.quaternion = tmp3
        return this
    }

    override fun setRotationFromAxis(ax: Double, ay: Double, az: Double, angle: Double): IShape {
        Rotation.dQFromAxisAndAngle(tmp3, ax, ay, az, angle)
        geom.quaternion = tmp3
        return this
    }

    override fun setRotationFromAxis(axis: IVec3, angle: Float): IShape {
        tmp4.set(axis.x.toDouble(), axis.y.toDouble(), axis.z.toDouble())
        Rotation.dQFromAxisAndAngle(tmp3, tmp4, angle.toDouble())
        geom.quaternion = tmp3
        return this
    }

    override fun setPosition(x: Float, y: Float, z: Float): IShape {
        geom.setPosition(x.toDouble(), y.toDouble(), z.toDouble())
        return this
    }

    override fun setPosition(x: Double, y: Double, z: Double): IShape {
        geom.setPosition(x, y, z)
        return this
    }

    override fun destroy() {
        geom.destroy()
    }

    companion object {
        val tmp1 = Vec3()
        val tmp2 = Vec4()
        val tmp3 = DQuaternion()
        val tmp4 = DVector3()
    }
}