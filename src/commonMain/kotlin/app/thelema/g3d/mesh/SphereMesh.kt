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

package app.thelema.g3d.mesh

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.math.MATH
import kotlin.math.abs

class SphereMesh(): MeshBuilderAdapter() {
    constructor(block: SphereMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "SphereMesh"

    var radius: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var hDivisions: Int = 16
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var vDivisions: Int = 16
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    override fun getIndicesCount(): Int = 6 * (hDivisions + 1) * (vDivisions + 1)

    override fun getVerticesCount(): Int = (hDivisions + 1) * (vDivisions + 1) + 1

    fun setSize(radius: Float) {
        this.radius = radius
    }

    fun setDivisions(horizontal: Int, vertical: Int) {
        hDivisions = horizontal
        vDivisions = vertical
    }

    override fun applyVertices() {
        preparePositions {
            val hNum = hDivisions + 1
            val vNum = vDivisions + 1

            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)

            // https://en.wikipedia.org/wiki/Spherical_coordinate_system
            for (yi in 0 until vNum) {
                val v = yi.toFloat() / vDivisions
                val zenith = v * MATH.PI

                val yn = MATH.cos(zenith)
                val xzWeight = 1f - abs(yn)
                val y = radius * yn
                val zenithSinR = radius * MATH.sin(zenith)

                for (xi in 0 until hNum) {
                    val u = xi.toFloat() / hDivisions
                    val azimuth = u * MATH.PI2

                    val xn = MATH.cos(azimuth)
                    val zn = MATH.sin(azimuth)
                    val x = zenithSinR * xn
                    val z = zenithSinR * zn

                    putFloatsNext(x, y, z)
                    uvs?.putFloatsNext(u, v)
                    normals?.putFloatsNext(xn * xzWeight, yn, zn * xzWeight)
                }
            }
        }
    }

    override fun applyIndices() {
        prepareIndices {
            var ringIndex = 1
            while (ringIndex < vDivisions + 2) {
                var curRingVertexIndex = hDivisions * ringIndex
                var prevRingVertexIndex = hDivisions * (ringIndex - 1)
                val num = hDivisions * (ringIndex + 1) + 1
                while (curRingVertexIndex < num) {
                    putIndices((curRingVertexIndex + 1), prevRingVertexIndex, (prevRingVertexIndex + 1))
                    putIndices(curRingVertexIndex, prevRingVertexIndex, (curRingVertexIndex + 1))
                    curRingVertexIndex += 1
                    prevRingVertexIndex += 1
                }
                ringIndex++
            }
        }
    }
}

fun IEntity.sphereMesh(block: SphereMesh.() -> Unit) = component(block)
fun IEntity.sphereMesh() = component<SphereMesh>()