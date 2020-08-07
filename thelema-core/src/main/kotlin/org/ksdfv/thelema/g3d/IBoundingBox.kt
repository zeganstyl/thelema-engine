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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.mesh.IVertexBuffer
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
interface IBoundingBox {
    val min: IVec3
    val max: IVec3

    fun addPoint(x: Float, y: Float, z: Float) {
        min.x = min(min.x, x)
        min.y = min(min.y, y)
        min.z = min(min.z, z)

        max.x = max(max.x, x)
        max.y = max(max.y, y)
        max.z = max(max.z, z)
    }

    fun addVertices(vertices: IVertexBuffer, aPositionName: String = "POSITION") {
        val offset = vertices.vertexInputs.byteOffsetOf(aPositionName) / 4

        val floatsPerVertex = vertices.vertexInputs.floatsPerVertex
        val numFloats = vertices.size * floatsPerVertex
        val buffer = vertices.floatBuffer
        var i = offset
        while (i < numFloats) {
            addPoint(buffer[i], buffer[i+1], buffer[i+2])
            i += floatsPerVertex
        }
    }

    fun setMesh(mesh: IMesh, aPositionName: String = "POSITION") {
        reset()
        val vertices = mesh.vertices
        if (vertices != null) addVertices(vertices, aPositionName)
    }

    fun setMeshes(meshes: List<IMesh>, aPositionName: String = "POSITION") {
        reset()

        for (i in meshes.indices) {
            val vertices = meshes[i].vertices
            if (vertices != null) addVertices(vertices, aPositionName)
        }
    }

    fun reset() {
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        max.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
    }
}