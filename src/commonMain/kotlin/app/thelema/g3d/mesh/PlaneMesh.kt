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
import app.thelema.gl.IVertexAttribute
import app.thelema.math.IVec3
import app.thelema.math.Vec3

class PlaneMesh(): MeshBuilderAdapter() {
    constructor(block: PlaneMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "PlaneMesh"

    var width: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var height: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var hDivisions: Int = 1
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var vDivisions: Int = 1
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var heightProvider: (hIndex: Int, vIndex: Int) -> Float = { _, _ -> 0f }

    var normal: IVec3 = Vec3(0f, 1f, 0f)

    override fun getVerticesCount(): Int = (hDivisions + 1) * (vDivisions + 1)

    override fun getIndicesCount(): Int = 6 * hDivisions * vDivisions

    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    fun setSize(value: Float) = setSize(value, value)

    fun setDivisions(horizontal: Int, vertical: Int) {
        hDivisions = horizontal
        vDivisions = vertical
    }

    fun setDivisions(divisions: Int) = setDivisions(divisions, divisions)

    override fun applyVertices() {
        val xNum = hDivisions + 1
        val yNum = vDivisions + 1

        val positions = mesh.getAttribute(builder.positionName)
        positions.rewind()

        val uvs = mesh.getAttributeOrNull(builder.uvName)
        uvs?.rewind()

        val normals = mesh.getAttributeOrNull(builder.normalName)
        normals?.rewind()

        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f

        val xStart = -halfWidth
        val xStep = width / hDivisions
        val uStep = xStep / width

        val yStart = -halfHeight
        val yStep = height / vDivisions
        val vStep = yStep / height

        val pointFunction = when {
            normal.isEqual(0f, 1f, 0f) -> ::putPositionsAsNormalY
            normal.isEqual(1f, 0f, 0f) -> ::putPositionsAsNormalX
            normal.isEqual(0f, 0f, 1f) -> ::putPositionsAsNormalZ
            else -> ::putPositionsByNormal
        }

        var y = yStart
        var v = 0f
        var iy = 0
        while (iy < yNum) {

            var x = xStart
            var u = 0f
            var ix = 0
            while (ix < xNum) {
                pointFunction(positions, uvs, normals, x, y, ix, iy, u, v)

                u += uStep
                x += xStep
                ix++
            }

            v += vStep
            y += yStep
            iy++
        }

        positions.rewind()
        uvs?.rewind()
        normals?.rewind()

        positions.buffer.requestBufferUploading()
        uvs?.buffer?.requestBufferUploading()
        normals?.buffer?.requestBufferUploading()
    }

    private fun putPositionsAsNormalX(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        x: Float,
        y: Float,
        ix: Int,
        iy: Int,
        u: Float,
        v: Float
    ) {
        positions.putFloatsNext(heightProvider(ix, iy), y, x)
        uvs?.putFloatsNext(u, v)
        normals?.putFloatsNext(1f, 0f, 0f)
    }

    private fun putPositionsAsNormalY(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        x: Float,
        y: Float,
        ix: Int,
        iy: Int,
        u: Float,
        v: Float
    ) {
        positions.putFloatsNext(x, heightProvider(ix, iy), y)
        uvs?.putFloatsNext(u, v)
        normals?.putFloatsNext(0f, 1f, 0f)
    }

    private fun putPositionsAsNormalZ(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        x: Float,
        y: Float,
        ix: Int,
        iy: Int,
        u: Float,
        v: Float
    ) {
        positions.putFloatsNext(x, y, heightProvider(ix, iy))
        uvs?.putFloatsNext(u, v)
        normals?.putFloatsNext(0f, 0f, 1f)
    }

    private fun putPositionsByNormal(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        x: Float,
        y: Float,
        ix: Int,
        iy: Int,
        u: Float,
        v: Float
    ) {
        positions.putFloatsNext(x, heightProvider(ix, iy), y)
        uvs?.putFloatsNext(u, v)
        normals?.putFloatsNext(normal.x, normal.y, normal.z)
    }

    override fun applyIndices() {
        prepareIndices {
            val xQuads = hDivisions
            val yQuads = vDivisions

            var v0 = 0
            var v1 = xQuads + 1
            var v2 = v0 + 1
            var v3 = v1 + 1

            var ri = 0
            while (ri < yQuads) {

                var ci = 0
                while (ci < xQuads) {
                    putIndices(
                        v0, v1, v2,
                        v1, v3, v2
                    )

                    v0 += 1
                    v1 += 1
                    v2 += 1
                    v3 += 1
                    ci++
                }

                v0 += 1
                v1 += 1
                v2 += 1
                v3 += 1
                ri++
            }
        }
    }
}

fun IEntity.planeMesh(block: PlaneMesh.() -> Unit) = component(block)
fun IEntity.planeMesh() = component<PlaneMesh>()