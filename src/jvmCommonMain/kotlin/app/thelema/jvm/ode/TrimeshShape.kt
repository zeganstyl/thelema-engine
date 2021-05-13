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
import app.thelema.gl.IMesh
import app.thelema.phys.IShape
import app.thelema.phys.ITrimeshShape
import org.ode4j.ode.DTriMesh
import org.ode4j.ode.DTriMeshData
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class TrimeshShape: ITrimeshShape {
    var trimeshData: DTriMeshData? = null

    var trimesh: DTriMesh? = null

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = trimesh
        }

    override var shape: IShape = Shape().also { it.geom = trimesh }

    override var mesh: IMesh? = null

    var positionAttributeName: String = "POSITION"

    override fun startSimulation() {
        val mesh = mesh
        if (mesh != null) {
            val positions = mesh.getAttributeOrNull(positionAttributeName)
            if (positions != null) {
                trimeshData = OdeHelper.createTriMeshData().apply {
                    val verticesCount = positions.buffer.verticesCount
                    val floatsCount = verticesCount * 3
                    val positionsArray = FloatArray(floatsCount)
                    positions.rewind()
                    var i = 0
                    while (i < floatsCount) {
                        positionsArray[i] = positions.getFloat(0)
                        positionsArray[i+1] = positions.getFloat(4)
                        positionsArray[i+2] = positions.getFloat(8)
                        positions.nextVertex()
                        i += 3
                    }
                    positions.rewind()

                    val indices = mesh.indices
                    val indicesArray = if (indices == null) {
                        IntArray(verticesCount) { it }
                    } else {
                        indices.rewind()
                        IntArray(indices.size) {
                            indices.getIndexNext()
                        }.also { indices.rewind() }
                    }

                    build(positionsArray, indicesArray)
                    preprocess()
                }

                trimesh = OdeHelper.createTriMesh(null, trimeshData!!, null, null, null)
            }
        }
    }

    override fun endSimulation() {
        trimesh?.destroy()
        trimesh = null
        trimeshData?.destroy()
        trimeshData = null
    }
}