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

import org.ksdfv.thelema.phys.IShape
import org.ksdfv.thelema.phys.ITrimeshShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeTrimeshShape(world: OdePhysicsWorld, vertices: FloatArray, indices: IntArray): ITrimeshShape, OdeGeom() {
    val trimeshData = OdeHelper.createTriMeshData().apply {
        build(vertices, indices)
        preprocess()
    }

    val trimesh = OdeHelper.createTriMesh(world.space, trimeshData, null, null, null)

    override val geom: DGeom
        get() = trimesh

    override var friction: Float = 1f

    override var userObject: Any = this

    override val shapeType: Int
        get() = IShape.TriMeshType

    init {
        geom.data = this
    }

    override fun destroy() {
        super.destroy()
        trimeshData.destroy()
    }
}