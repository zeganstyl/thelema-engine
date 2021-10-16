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

import app.thelema.math.IVec3
import app.thelema.phys.IBodyContact
import app.thelema.phys.IRigidBody
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom

/** @author zeganstyl */
class BodyContact(
    override val body1: RigidBody,
    override val body2: RigidBody,
    override val position: IVec3,
    override val normal: IVec3,
    override val depth: Float
): IBodyContact {
    constructor(
        body1: RigidBody,
        body2: RigidBody,
        contactGeom: DContactGeom
    ): this(
        body1,
        body2,
        contactGeom.pos.toVec3(),
        contactGeom.normal.toVec3(),
        contactGeom.depth.toFloat()
    )

    constructor(body1: RigidBody, body2: RigidBody, contactGeom: DContact): this(body1, body2, contactGeom.contactGeom)

    override fun equals(other: Any?): Boolean {
        other as BodyContact
        return other.body1 == body1 && other.body2 == body2
    }

    // https://stackoverflow.com/questions/24262897/integer-pair-add-to-hashset-java
    override fun hashCode(): Int = contactHash(body1, body2)

    companion object {
        fun contactHash(body1: IRigidBody, body2: IRigidBody): Int = body1.hashCode() * 31 + body2.hashCode()
    }
}