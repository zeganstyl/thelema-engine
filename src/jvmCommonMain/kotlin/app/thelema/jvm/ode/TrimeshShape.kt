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
import app.thelema.utils.LOG
import org.ode4j.ode.DMass
import org.ode4j.ode.DTriMesh
import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class TrimeshShape: SpecificShape<DTriMesh>(), ITrimeshShape {
    var trimeshData: DTriMeshData? = null

    override var mesh: IMesh? = null
        set(value) {
            println(value?.path)
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
//        override fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) { setupBody() }
//        override fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) { setupBody() }
//        override fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) { setupBody() }
//        override fun inheritedMeshChanged(mesh: IMesh, newInheritedMesh: IMesh?) { setupBody() }
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
        if (mesh == null) LOG.error("$path: Mesh is null")
        mesh?.also { mesh ->
            val positions = mesh.getAttributeOrNull(mesh.positionsName)
            if (positions == null) {
                LOG.error("$path: Positions attribute \"${mesh.positionsName}\" is not found in ${mesh.path}")
                return null
            }

            val verticesCount = positions.count

            if (verticesCount <= 0) {
                LOG.error("$path: Vertices count must be > 0 in ${mesh.path}")
                return null
            }

            val indicesArray = mesh.indices?.toIntArray() ?: IntArray(verticesCount) { it }

            if (indicesArray.isEmpty()) {
                LOG.error("$path: Indices size must be > 0 in ${mesh.path}")
                return null
            }

            val floatsCount = verticesCount * 3
            val positionsArray = FloatArray(floatsCount)
            positions.rewind {
                var i = 0
                while (i < floatsCount) {
                    positionsArray[i] = getFloat(0)
                    positionsArray[i+1] = getFloat(4)
                    positionsArray[i+2] = getFloat(8)
                    nextVertex()
                    i += 3
                }
            }

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
