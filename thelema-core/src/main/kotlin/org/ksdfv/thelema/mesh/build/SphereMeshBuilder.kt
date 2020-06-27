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

package org.ksdfv.thelema.mesh.build

import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.IMesh
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author zeganstyl
 */
class SphereMeshBuilder() : MeshBuilder() {
    constructor(block: SphereMeshBuilder.() -> Unit): this() { block(this) }

    var radius = 1f
    var horizontalDivisions = 16
    var verticalDivisions = 16

    override fun build(out: IMesh): IMesh {
        out.vertices = createVerticesFloat((horizontalDivisions + 1) * (verticalDivisions + 1)) {
            // https://en.wikipedia.org/wiki/Spherical_coordinate_system

            for (yi in 0 until verticalDivisions + 1) {
                val zenithPercent = yi.toFloat() / verticalDivisions
                val zenith = zenithPercent * PI.toFloat()

                val y = radius * cos(zenith)

                for (xi in 0 until horizontalDivisions + 1) {
                    val azimuthPercent = xi.toFloat() / horizontalDivisions
                    val azimuth = azimuthPercent * PI.toFloat() * 2f

                    val x = radius * sin(zenith) * cos(azimuth)
                    val z = radius * sin(zenith) * sin(azimuth)
                    if (normals) normal.set(x, y, z).nor()

                    put(x, y, z)
                    if (textureCoordinates) put(azimuthPercent * textureCoordinatesScale, zenithPercent * textureCoordinatesScale)
                    if (normals) put(normal.x, normal.y, normal.z)
                }
            }
        }

        out.indices = createIndicesShort(6 * (horizontalDivisions + 1) * (verticalDivisions + 1)) {
            var ringIndex = 1
            while (ringIndex < verticalDivisions + 2) {

                var curRingVertexIndex = horizontalDivisions * ringIndex
                var prevRingVertexIndex = horizontalDivisions * (ringIndex - 1)
                while (curRingVertexIndex < horizontalDivisions * (ringIndex + 1) + 1) {
                    put((curRingVertexIndex + 1).toShort(), prevRingVertexIndex.toShort(), (prevRingVertexIndex + 1).toShort())
                    put(curRingVertexIndex.toShort(), prevRingVertexIndex.toShort(), (curRingVertexIndex + 1).toShort())
                    curRingVertexIndex += 1
                    prevRingVertexIndex += 1
                }

                ringIndex++
            }
        }

        return super.build(out)
    }

    companion object {
        val normal = Vec3()
    }
}
