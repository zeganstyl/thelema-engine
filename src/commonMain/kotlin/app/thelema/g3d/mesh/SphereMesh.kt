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
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.gl.*
import app.thelema.json.IJsonObject
import app.thelema.math.MATH
import app.thelema.math.Vec3

class SphereMesh(): IEntityComponent {
    constructor(block: SphereMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        if (isMeshUpdateRequested) updateMesh()
    }

    override val componentName: String
        get() = "SphereMesh"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            builder = value?.component() ?: MeshBuilder()
        }

    var radius: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var hDivisions: Int = 16
    var vDivisions: Int = 16

    var isMeshUpdateRequested = true

    var builder = MeshBuilder()

    val mesh: IMesh
        get() = builder.mesh

    fun setSize(radius: Float) {
        this.radius = radius
    }

    fun setDivisions(horizontal: Int, vertical: Int) {
        hDivisions = horizontal
        vDivisions = vertical
    }

    private fun applyVertices() {
        val pi = 3.141592653589793f
        val pi2 = pi * 2f
        val hNum = hDivisions + 1
        val vNum = vDivisions + 1

        val uvs = if (builder.uvs) mesh.getAttributeOrNull(builder.uvName) else null
        val normals = if (builder.normals) mesh.getAttributeOrNull(builder.normalName) else null

        mesh.getAttribute(builder.positionName) {
            uvs?.rewind()
            normals?.rewind()
            rewind()

            // https://en.wikipedia.org/wiki/Spherical_coordinate_system
            for (yi in 0 until vNum) {
                val v = yi.toFloat() / vDivisions
                val zenith = v * pi

                val yn = MATH.cos(zenith)
                val y = radius * yn
                val zenithSinR = radius * MATH.sin(zenith)

                for (xi in 0 until hNum) {
                    val u = xi.toFloat() / hDivisions
                    val azimuth = u * pi2

                    val xn = MATH.cos(azimuth)
                    val zn = MATH.sin(azimuth)
                    val x = zenithSinR * xn
                    val z = zenithSinR * zn

                    putFloatsNext(x, y, z)
                    uvs?.putFloatsNext(u, v)
                    normals?.putFloatsNext(xn, yn, zn)
                }
            }

            uvs?.rewind()
            normals?.rewind()
            rewind()
            buffer.requestBufferLoading()
        }
    }

    private fun applyIndices(buffer: IIndexBuffer) {
        val newSize = 6 * (hDivisions + 1) * (vDivisions + 1)
        if (newSize != buffer.size) buffer.initIndexBuffer(newSize) {}

        buffer.bytes.rewind()

        var ringIndex = 1
        while (ringIndex < vDivisions + 2) {
            var curRingVertexIndex = hDivisions * ringIndex
            var prevRingVertexIndex = hDivisions * (ringIndex - 1)
            val num = hDivisions * (ringIndex + 1) + 1
            while (curRingVertexIndex < num) {
                buffer.bytes.putShorts((curRingVertexIndex + 1), prevRingVertexIndex, (prevRingVertexIndex + 1))
                buffer.bytes.putShorts(curRingVertexIndex, prevRingVertexIndex, (curRingVertexIndex + 1))
                curRingVertexIndex += 1
                prevRingVertexIndex += 1
            }

            ringIndex++
        }

        buffer.bytes.rewind()
        buffer.requestBufferLoading()
    }

    fun updateMesh() {
        mesh.verticesCount = (hDivisions + 1) * (vDivisions + 1)

        if (mesh.vertexBuffers.isEmpty()) {
            mesh.addVertexBuffer {
                addAttribute(3, builder.positionName)
                if (builder.uvs) addAttribute(2, builder.uvName)
                if (builder.normals) addAttribute(3, builder.normalName)
                initVertexBuffer(mesh.verticesCount)
            }
        }
        applyVertices()

        if (mesh.indices == null) {
            mesh.setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(6 * (hDivisions + 1) * (vDivisions + 1)) {}
            }
        }
        applyIndices(mesh.indices!!)

        isMeshUpdateRequested = false
    }

    fun requestMeshUpdate() {
        isMeshUpdateRequested = true
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        if (isMeshUpdateRequested) updateMesh()
    }
}