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
import app.thelema.math.MATH
import app.thelema.math.Vec2
import app.thelema.math.Vec3

/**
 * @author zeganstyl
 */
class CylinderMesh(): MeshBuilderAdapter() {
    constructor(block: CylinderMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "CylinderMesh"

    var radius: Float = 1f
    var length: Float = 1f
    var divisions: Int = 8
    var cap: Boolean = true

    var axis: IVec3 = Vec3(0f, 1f, 0f)

    val tmp = Vec2()

    override fun getVerticesCount(): Int = 2 * (if (cap) divisions + 2 else divisions)
    override fun getIndicesCount(): Int = 6 * (if (cap) divisions * 2 else divisions)

    override fun applyVertices() {
        preparePositions {
            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)

            val y2 = length * 0.5f
            val y1 = -y2

            val putPosNor = when {
                axis.isEqual(0f, 1f, 0f) -> ::putPosAlongY
                axis.isEqual(0f, 0f, 1f) -> ::putPosAlongZ
                axis.isEqual(1f, 0f, 0f) -> ::putPosAlongX
                else -> ::putPosAlongY
            }

            for (i in 0 until divisions) {
                val anglePercent = (i.toFloat() / divisions)
                val angle = anglePercent * MATH.PI2

                val nx = MATH.cos(angle)
                val nz = MATH.sin(angle)
                val x = nx * radius
                val z = nz * radius

                putPosNor(this, normals, x, y1, y2, z, nx, 0f, 0f, nz)

                if (uvs != null) {
                    uvs.putFloatsNext(anglePercent, 0f)
                    uvs.putFloatsNext(anglePercent, 1f)
                }
            }

            // last two vertices must be with u = 1f texture coordinate
            val x = 1f * radius
            val z = 0f * radius

            putPosNor(this, normals, x, y1, y2, z, 1f, 0f, 0f, 0f)

            if (uvs != null) {
                uvs.putFloatsNext(1f, 0f)
                uvs.putFloatsNext(1f, 1f)
            }

            if (cap) {
                putPosNor(this, normals, 0f, y1, y2, 0f, 0f, -1f, 1f, 0f)

                if (uvs != null) {
                    uvs.putFloatsNext(0f, 0f)
                    uvs.putFloatsNext(0f, 0f)
                }
            }
        }
    }

    private fun putPosAlongX(
        positions: IVertexAttribute,
        normals: IVertexAttribute?,
        x: Float,
        y1: Float,
        y2: Float,
        z: Float,
        nx: Float,
        ny1: Float,
        ny2: Float,
        nz: Float
    ) {
        positions.putFloatsNext(y1, x, z)
        positions.putFloatsNext(y2, x, z)
        if (normals != null) {
            normals.putFloatsNext(ny1, nx, nz)
            normals.putFloatsNext(ny2, nx, nz)
        }
    }

    private fun putPosAlongY(
        positions: IVertexAttribute,
        normals: IVertexAttribute?,
        x: Float,
        y1: Float,
        y2: Float,
        z: Float,
        nx: Float,
        ny1: Float,
        ny2: Float,
        nz: Float
    ) {
        positions.putFloatsNext(x, y1, z)
        positions.putFloatsNext(x, y2, z)
        if (normals != null) {
            normals.putFloatsNext(nx, ny1, nz)
            normals.putFloatsNext(nx, ny2, nz)
        }
    }

    private fun putPosAlongZ(
        positions: IVertexAttribute,
        normals: IVertexAttribute?,
        x: Float,
        y1: Float,
        y2: Float,
        z: Float,
        nx: Float,
        ny1: Float,
        ny2: Float,
        nz: Float
    ) {
        positions.putFloatsNext(x, z, y1)
        positions.putFloatsNext(x, z, y2)
        if (normals != null) {
            normals.putFloatsNext(nx, nz, ny1)
            normals.putFloatsNext(nx, nz, ny2)
        }
    }

    override fun applyIndices() {
        prepareIndices {
            val num = 2 * divisions - 2
            var i = 0
            while (i < num) {
                putIndices(i, (i + 1), (i + 2))
                putIndices((i + 2), (i + 1), (i + 3))
                i = (i + 2)
            }

            putIndices(i, (i + 1), 0)
            putIndices(0, (i + 1), 1)

            if (cap) {
                val bottom = divisions * 2
                val top = bottom + 1
                i = 0
                while (i < num) {
                    putIndices(i, bottom, (i + 2))
                    putIndices((i + 1), top, (i + 3))
                    i = (i + 2)
                }
            }
        }
    }
}

fun IEntity.cylinderMesh(block: CylinderMesh.() -> Unit) = component(block)
fun IEntity.cylinderMesh() = component<CylinderMesh>()