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

package app.thelema.g3d.terrain

import app.thelema.g3d.mesh.MeshBuilderAdapter
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IVertexAttribute
import app.thelema.gl.IndexBuffer

/** Frame with LOD transition from 2 vertices to 1. You can use it in conjunction with [TerrainTileMesh] */
class TerrainLodFrame2to1Mesh(): MeshBuilderAdapter() {
    constructor(block: TerrainLodFrame2to1Mesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "TerrainLodFrame2to1Mesh"

    private var vertexCounter = 0

    var top: Boolean = true
    var right: Boolean = true
    var bottom: Boolean = true
    var left: Boolean = true

    var frameSize: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var outerLodDivisions: Int = 1
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    private var leftOuterStart = 0
    private var leftInnerStart = 0
    private var rightOuterStart = 0
    private var rightInnerStart = 0
    private var topOuterStart = 0
    private var topInnerStart = 0
    private var bottomInnerStart = 0
    private var bottomOuterStart = 0
    private var leftTopCorner = 0
    private var rightTopCorner = 0
    private var leftBottomCorner = 0
    private var rightBottomCorner = 0

    fun setSize(size: Float) {
        frameSize = size
    }

    fun setDivisions(divisions: Int) {
        outerLodDivisions = divisions
    }

    override fun getVerticesCount(): Int = outerLodDivisions * 2 * 8 + 4

    override fun getIndicesCount(): Int = outerLodDivisions * 48 + 24

    override fun applyVertices() {
        preparePositions {
            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)
            val verticesPerLine = outerLodDivisions * 2
            val quadSize = frameSize * 0.5f / outerLodDivisions

            leftOuterStart = vLine(this, uvs, normals, verticesPerLine, quadSize, 0f, quadSize)
            leftInnerStart = vLine(this, uvs, normals, verticesPerLine, quadSize, quadSize, quadSize)

            rightInnerStart = vLine(this, uvs, normals, verticesPerLine, quadSize, frameSize - quadSize, quadSize)
            rightOuterStart = vLine(this, uvs, normals, verticesPerLine, quadSize, frameSize, quadSize)

            topOuterStart = hLine(this, uvs, normals, verticesPerLine, quadSize, quadSize, 0f)
            topInnerStart = hLine(this, uvs, normals, verticesPerLine, quadSize, quadSize, quadSize)

            bottomInnerStart = hLine(this, uvs, normals, verticesPerLine, quadSize, quadSize, frameSize - quadSize)
            bottomOuterStart = hLine(this, uvs, normals, verticesPerLine, quadSize, quadSize, frameSize)

            leftTopCorner = corner(this, uvs, normals, 0f, 0f)
            rightTopCorner = corner(this, uvs, normals, frameSize, 0f)
            leftBottomCorner = corner(this, uvs, normals, 0f, frameSize)
            rightBottomCorner = corner(this, uvs, normals, frameSize, frameSize)
        }
    }

    override fun applyIndices() {
        prepareIndices {
            val divs = outerLodDivisions * 2 - 2

            // corners
            linkLineCorners(this, leftTopCorner, rightTopCorner, topInnerStart, topOuterStart, divs, if (top) 1 else 0, 1)
            linkLineCorners(this, leftTopCorner, leftBottomCorner, leftInnerStart, leftOuterStart, divs, if (left) 1 else 0, 0)
            linkLineCorners(this, rightTopCorner, rightBottomCorner, rightInnerStart, rightOuterStart, divs, if (right) 1 else 0, 1)
            linkLineCorners(this, leftBottomCorner, rightBottomCorner, bottomInnerStart, bottomOuterStart, divs, if (bottom) 1 else 0, 0)

            // edges
            if (top) linkTransitionEdge(this, topOuterStart, topInnerStart, 0) else linkEdge(this, topOuterStart, topInnerStart, 0)
            if (left) linkTransitionEdge(this, leftOuterStart, leftInnerStart, 1) else linkEdge(this, leftOuterStart, leftInnerStart, 1) // 1
            if (bottom) linkTransitionEdge(this, bottomOuterStart, bottomInnerStart, 1) else linkEdge(this, bottomOuterStart, bottomInnerStart, 1) // 1
            if (right) linkTransitionEdge(this, rightOuterStart, rightInnerStart, 0) else linkEdge(this, rightOuterStart, rightInnerStart, 0)
        }
    }

    fun setSideFlags(left: Boolean, top: Boolean, right: Boolean, bottom: Boolean): TerrainLodFrame2to1Mesh {
        this.top = top
        this.left = left
        this.right = right
        this.bottom = bottom
        return this
    }

    private fun hLine(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        verticesNum: Int,
        step: Float,
        xOffset: Float,
        zOffset: Float
    ): Int {
        val counter = vertexCounter

        val uStep = step / frameSize
        val v = zOffset / frameSize

        var x = xOffset
        var u = (xOffset + frameSize * 0.5f) / frameSize

        for (i in 0 until verticesNum) {
            positions.putFloatsNext(x, 0f, zOffset)
            uvs?.putFloatsNext(u, v)
            normals?.putFloatsNext(0f, 1f, 0f)
            vertexCounter++
            u += uStep
            x += step
        }

        return counter
    }

    private fun vLine(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        verticesNum: Int,
        step: Float,
        xOffset: Float,
        zOffset: Float
    ): Int {
        val counter = vertexCounter

        val vStep = step / frameSize
        val u = xOffset / frameSize

        var z = zOffset
        var v = (step + frameSize * 0.5f) / frameSize

        for (i in 0 until verticesNum) {
            positions.putFloatsNext(xOffset, 0f, z)
            uvs?.putFloatsNext(u, v)
            normals?.putFloatsNext(0f, 1f, 0f)
            vertexCounter++
            v += vStep
            z += step
        }

        return counter
    }

    private fun corner(
        positions: IVertexAttribute,
        uvs: IVertexAttribute?,
        normals: IVertexAttribute?,
        x: Float,
        z: Float
    ): Int {
        val counter = vertexCounter
        positions.putFloatsNext(x, 0f, z)
        uvs?.putFloatsNext(x / frameSize, z / frameSize)
        normals?.putFloatsNext(0f, 1f, 0f)
        vertexCounter++
        return counter
    }

    private fun linkTransitionEdge(out: IIndexBuffer, outerStart: Int, innerStart: Int, order: Int) {
        var outerQuadsNum = outerLodDivisions - 1
        var outer = outerStart + 1
        var inner = innerStart + 1

        if (order == 1) {
            for (i in 0 until outerQuadsNum) {
                out.putIndices(outer, inner, (inner - 1))
                out.putIndices(inner, outer, (inner + 1))
                outer += 2
                inner += 2
            }

            outerQuadsNum = outerLodDivisions - 2
            outer = outerStart + 3
            inner = innerStart + 3
            for (i in 0 until outerQuadsNum) {
                out.putIndices(outer, (inner - 1), (outer - 2))
                outer += 2
                inner += 2
            }
        } else {
            for (i in 0 until outerQuadsNum) {
                out.putIndices(inner, outer, (inner - 1))
                out.putIndices(outer, inner, (inner + 1))
                outer += 2
                inner += 2
            }

            outerQuadsNum = outerLodDivisions - 2
            outer = outerStart + 3
            inner = innerStart + 3
            for (i in 0 until outerQuadsNum) {
                out.putIndices((inner - 1), outer, (outer - 2))
                outer += 2
                inner += 2
            }
        }
    }

    private fun linkEdge(out: IIndexBuffer, line1Start: Int, line2Start: Int, order: Int) {
        val quadsNum = outerLodDivisions * 2 - 2
        var line1 = line1Start
        var line2 = line2Start
        if (order == 1) {
            for (i in 0 until quadsNum) {
                out.putIndices(
                    line1, (line1 + 1), line2,
                    (line2 + 1), line2, (line1 + 1)
                )
                line1++
                line2++
            }
        } else {
            for (i in 0 until quadsNum) {
                out.putIndices(
                    (line1 + 1), line1, line2,
                    (line2 + 1), (line1 + 1), line2
                )
                line1++
                line2++
            }
        }
    }

    private fun linkLineCorners(out: IIndexBuffer, firstCorner: Int, lastCorner: Int, innerStart: Int, outerStart: Int, divs: Int, add: Int, order: Int) {
        if (order == 0) {
            out.putIndices(innerStart, firstCorner, (outerStart + add))
            out.putIndices(lastCorner, (innerStart + divs), (outerStart + divs - add))
        } else {
            out.putIndices(firstCorner, innerStart, (outerStart + add))
            out.putIndices((innerStart + divs), lastCorner, (outerStart + divs - add))
        }
    }

    fun buildIndices() = IndexBuffer {
        initIndexBuffer(outerLodDivisions * 48 + 24) {
            val divs = outerLodDivisions * 2 - 2

            // corners
            linkLineCorners(this, leftTopCorner, rightTopCorner, topInnerStart, topOuterStart, divs, if (top) 1 else 0, 1)
            linkLineCorners(this, leftTopCorner, leftBottomCorner, leftInnerStart, leftOuterStart, divs, if (left) 1 else 0, 0)
            linkLineCorners(this, rightTopCorner, rightBottomCorner, rightInnerStart, rightOuterStart, divs, if (right) 1 else 0, 1)
            linkLineCorners(this, leftBottomCorner, rightBottomCorner, bottomInnerStart, bottomOuterStart, divs, if (bottom) 1 else 0, 0)

            // edges
            if (top) linkTransitionEdge(this, topOuterStart, topInnerStart, 0) else linkEdge(this, topOuterStart, topInnerStart, 0)
            if (left) linkTransitionEdge(this, leftOuterStart, leftInnerStart, 1) else linkEdge(this, leftOuterStart, leftInnerStart, 1)
            if (bottom) linkTransitionEdge(this, bottomOuterStart, bottomInnerStart, 1) else linkEdge(this, bottomOuterStart, bottomInnerStart, 1)
            if (right) linkTransitionEdge(this, rightOuterStart, rightInnerStart, 0) else linkEdge(this, rightOuterStart, rightInnerStart, 0)
        }
    }
}