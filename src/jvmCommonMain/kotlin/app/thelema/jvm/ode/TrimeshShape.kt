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
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.getSiblingOrNull
import app.thelema.gl.*
import app.thelema.phys.ITrimeshShape
import org.ode4j.ode.DMass
import org.ode4j.ode.DTriMesh
import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class TrimeshShape: SpecificShape<DTriMesh>(), ITrimeshShape {
    var trimeshData: DTriMeshData? = null

    override var mesh: IMesh? = null
        set(value) {
            if (field != value) {
                field?.removeMeshListener(meshListener)
                field = value
                value?.addMeshListener(meshListener)
            }
        }

    override var entityOrNull: IEntity?
        get() = super.entityOrNull
        set(value) {
            super.entityOrNull = value
            if (mesh == null) mesh = getSiblingOrNull()
        }

    private val meshListener = object : MeshListener {
        override fun bufferUploadedToGPU(buffer: IGLBuffer) { setupBody() }
        override fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) { setupBody() }
        override fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) { setupBody() }
        override fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) { setupBody() }
        override fun inheritedMeshChanged(mesh: IMesh, newInheritedMesh: IMesh?) { setupBody() }
    }

    private fun setupBody() {
        body?.also { body ->
            if (body.isSimulationRunning) {
                (body as RigidBody).setupBody()
            }
        }
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (mesh == null) mesh = getSiblingOrNull()
    }

    override fun setupMass(density: Double, mass: DMass) {
        mass.setTrimesh(density, geom!!)
    }

    override fun createGeom(): DTriMesh? {
        mesh?.also { mesh ->
            val positions = mesh.positions()

            val verticesCount = positions.count
            val floatsCount = verticesCount * 3
            val positionsArray = FloatArray(floatsCount)
            positions.prepare {
                var i = 0
                while (i < floatsCount) {
                    positionsArray[i] = positions.getFloat(0)
                    positionsArray[i+1] = positions.getFloat(4)
                    positionsArray[i+2] = positions.getFloat(8)
                    positions.nextVertex()
                    i += 3
                }
            }

            val indicesArray = mesh.indices?.toIntArray() ?: IntArray(verticesCount) { it }

            val trimeshData = OdeHelper.createTriMeshData()
            trimeshData.build(positionsArray, indicesArray)
            trimeshData.preprocess()
            this.trimeshData = trimeshData

            return OdeHelper.createTriMesh(getSpace(), trimeshData, null, null, null)
        }

        return null
    }

    override fun destroyGeom() {
        super.destroyGeom()
        trimeshData?.destroy()
        trimeshData = null
    }
}
