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

import app.thelema.data.IFloatData
import app.thelema.data.IShortData
import app.thelema.g3d.mesh.MeshBuilderOld
import app.thelema.gl.IMesh

/** Frame with LOD transition from 2 vertices to 1. You can use it in conjunction with [TerrainTileMeshBuilder] */
class TerrainLodFrame2to1MeshBuilder(
    var frameSize: Float = 1f,
    var outerLodDivisions: Int = 1
): MeshBuilderOld() {
    override var normals: Boolean
        get() = false
        set(_) = throw NotImplementedError()

    private var vertexCounter = 0

    var top: Boolean = true
    var right: Boolean = true
    var bottom: Boolean = true
    var left: Boolean = true

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

    fun setSideFlags(left: Boolean, top: Boolean, right: Boolean, bottom: Boolean): TerrainLodFrame2to1MeshBuilder {
        this.top = top
        this.left = left
        this.right = right
        this.bottom = bottom
        return this
    }

    private fun hLine(out: IFloatData, verticesNum: Int, step: Float, xOffset: Float, zOffset: Float): Int {
        val counter = vertexCounter

        val uStep = step / frameSize
        val v = zOffset / frameSize

        var x = xOffset
        var u = (xOffset + frameSize * 0.5f) / frameSize

        for (i in 0 until verticesNum) {
            out.put(x, 0f, zOffset)
            if (uv) out.put(u, v)
            vertexCounter++
            u += uStep
            x += step
        }

        return counter
    }

    private fun vLine(out: IFloatData, verticesNum: Int, step: Float, xOffset: Float, zOffset: Float): Int {
        val counter = vertexCounter

        val vStep = step / frameSize
        val u = xOffset / frameSize

        var z = zOffset
        var v = (step + frameSize * 0.5f) / frameSize

        for (i in 0 until verticesNum) {
            out.put(xOffset, 0f, z)
            if (uv) out.put(u, v)
            vertexCounter++
            v += vStep
            z += step
        }

        return counter
    }

    private fun corner(out: IFloatData, x: Float, z: Float): Int {
        val counter = vertexCounter
        out.put(x, 0f, z)
        if (uv) out.put(x / frameSize, z / frameSize)
        vertexCounter++
        return counter
    }

    private fun linkTransitionEdge(out: IShortData, outerStart: Int, innerStart: Int) {
        var outerQuadsNum = outerLodDivisions - 1
        var outer = outerStart + 1
        var inner = innerStart + 1
        for (i in 0 until outerQuadsNum) {
            out.put(outer.toShort(), inner.toShort(), (inner - 1).toShort())
            out.put(outer.toShort(), inner.toShort(), (inner + 1).toShort())
            outer += 2
            inner += 2
        }

        outerQuadsNum = outerLodDivisions - 2
        outer = outerStart + 3
        inner = innerStart + 3
        for (i in 0 until outerQuadsNum) {
            out.put(outer.toShort(), (inner - 1).toShort(), (outer - 2).toShort())
            outer += 2
            inner += 2
        }
    }

    private fun linkEdge(out: IShortData, line1Start: Int, line2Start: Int) {
        val quadsNum = outerLodDivisions * 2 - 2
        var line1 = line1Start
        var line2 = line2Start
        for (i in 0 until quadsNum) {
            out.put(
                line1.toShort(), (line1 + 1).toShort(), line2.toShort(),
                (line1 + 1).toShort(), (line2 + 1).toShort(), line2.toShort()
            )
            line1++
            line2++
        }
    }

    private fun linkLineCorners(out: IShortData, firstCorner: Int, lastCorner: Int, innerStart: Int, outerStart: Int, divs: Int, add: Int) {
        out.put(firstCorner.toShort(), innerStart.toShort(), (outerStart + add).toShort())
        out.put(lastCorner.toShort(), (innerStart + divs).toShort(), (outerStart + divs - add).toShort())
    }

    fun buildIndices() = createIndicesShort(outerLodDivisions * 48 + 24) {
        val divs = outerLodDivisions * 2 - 2

        // corners
        linkLineCorners(this, leftTopCorner, rightTopCorner, topInnerStart, topOuterStart, divs, if (top) 1 else 0)
        linkLineCorners(this, leftTopCorner, leftBottomCorner, leftInnerStart, leftOuterStart, divs, if (left) 1 else 0)
        linkLineCorners(this, rightTopCorner, rightBottomCorner, rightInnerStart, rightOuterStart, divs, if (right) 1 else 0)
        linkLineCorners(this, leftBottomCorner, rightBottomCorner, bottomInnerStart, bottomOuterStart, divs, if (bottom) 1 else 0)

        // edges
        if (top) linkTransitionEdge(this, topOuterStart, topInnerStart) else linkEdge(this, topOuterStart, topInnerStart)
        if (left) linkTransitionEdge(this, leftOuterStart, leftInnerStart) else linkEdge(this, leftOuterStart, leftInnerStart)
        if (bottom) linkTransitionEdge(this, bottomOuterStart, bottomInnerStart) else linkEdge(this, bottomOuterStart, bottomInnerStart)
        if (right) linkTransitionEdge(this, rightOuterStart, rightInnerStart) else linkEdge(this, rightOuterStart, rightInnerStart)
    }

    override fun build(out: IMesh): IMesh {
        val verticesPerLine = outerLodDivisions * 2

        out.addVertexBuffer(createVerticesFloat(verticesPerLine * 8 + 4) {
            val quadSize = frameSize * 0.5f / outerLodDivisions

            leftOuterStart = vLine(this, verticesPerLine, quadSize, 0f, quadSize)
            leftInnerStart = vLine(this, verticesPerLine, quadSize, quadSize, quadSize)

            rightInnerStart = vLine(this, verticesPerLine, quadSize, frameSize - quadSize, quadSize)
            rightOuterStart = vLine(this, verticesPerLine, quadSize, frameSize, quadSize)

            topOuterStart = hLine(this, verticesPerLine, quadSize, quadSize, 0f)
            topInnerStart = hLine(this, verticesPerLine, quadSize, quadSize, quadSize)

            bottomInnerStart = hLine(this, verticesPerLine, quadSize, quadSize, frameSize - quadSize)
            bottomOuterStart = hLine(this, verticesPerLine, quadSize, quadSize, frameSize)

            leftTopCorner = corner(this, 0f, 0f)
            rightTopCorner = corner(this, frameSize, 0f)
            leftBottomCorner = corner(this, 0f, frameSize)
            rightBottomCorner = corner(this, frameSize, frameSize)
        })

        out.indices = createIndicesShort(outerLodDivisions * 48 + 24) {
            val divs = verticesPerLine - 2

            // corners
            linkLineCorners(this, leftTopCorner, rightTopCorner, topInnerStart, topOuterStart, divs, if (top) 1 else 0)
            linkLineCorners(this, leftTopCorner, leftBottomCorner, leftInnerStart, leftOuterStart, divs, if (left) 1 else 0)
            linkLineCorners(this, rightTopCorner, rightBottomCorner, rightInnerStart, rightOuterStart, divs, if (right) 1 else 0)
            linkLineCorners(this, leftBottomCorner, rightBottomCorner, bottomInnerStart, bottomOuterStart, divs, if (bottom) 1 else 0)

            // edges
            if (top) linkTransitionEdge(this, topOuterStart, topInnerStart) else linkEdge(this, topOuterStart, topInnerStart)
            if (left) linkTransitionEdge(this, leftOuterStart, leftInnerStart) else linkEdge(this, leftOuterStart, leftInnerStart)
            if (bottom) linkTransitionEdge(this, bottomOuterStart, bottomInnerStart) else linkEdge(this, bottomOuterStart, bottomInnerStart)
            if (right) linkTransitionEdge(this, rightOuterStart, rightInnerStart) else linkEdge(this, rightOuterStart, rightInnerStart)
        }

        return out
    }
}