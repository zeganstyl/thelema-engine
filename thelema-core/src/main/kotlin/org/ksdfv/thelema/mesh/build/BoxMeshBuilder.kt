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

import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.mesh.IMesh

/**
 * @author zeganstyl
 */
class BoxMeshBuilder(
    var halfSizeX: Float = 1f,
    var halfSizeY: Float = 1f,
    var halfSizeZ: Float = 1f
): MeshBuilder() {
    override fun build(out: IMesh): IMesh {
        out.vertices = createVerticesFloat(24) {
            // front
            normal.set(1f, 0f, 0f)
            putV2(this, -1f, -1f,  1f, 0f, 0f)
            putV2(this, 1f, -1f,  1f, 1f, 0f)
            putV2(this, 1f, 1f,  1f, 1f, 1f)
            putV2(this, -1f, 1f,  1f, 0f, 1f)

            // top
            normal.set(0f, 1f, 0f)
            putV2(this, -1f, 1f,  1f, 0f, 0f)
            putV2(this, 1f, 1f,  1f, 1f, 0f)
            putV2(this, 1f, 1f,  -1f, 1f, 1f)
            putV2(this, -1f, 1f,  -1f, 0f, 1f)

            // back
            normal.set(0f, 0f, 1f).scl(-1f)
            putV2(this, 1f, -1f,  -1f, 0f, 0f)
            putV2(this, -1f, -1f,  -1f, 1f, 0f)
            putV2(this, -1f, 1f,  -1f, 1f, 1f)
            putV2(this, 1f, 1f,  -1f, 0f, 1f)

            // bottom
            normal.set(0f, 1f, 0f).scl(-1f)
            putV2(this, -1f, -1f,  -1f, 0f, 0f)
            putV2(this, 1f, -1f,  -1f, 1f, 0f)
            putV2(this, 1f, -1f,  1f, 1f, 1f)
            putV2(this, -1f, -1f,  1f, 0f, 1f)

            // left
            normal.set(1f, 0f, 0f).scl(-1f)
            putV2(this, -1f, -1f,  1f, 0f, 0f)
            putV2(this, -1f, -1f, -1f, 1f, 0f)
            putV2(this, -1f,  1f, -1f, 1f, 1f)
            putV2(this, -1f,  1f,  1f, 0f, 1f)

            // right
            normal.set(1f, 0f, 0f)
            putV2(this, 1f, -1f,  1f, 0f, 0f)
            putV2(this, 1f, -1f, -1f, 1f, 0f)
            putV2(this, 1f,  1f, -1f, 1f, 1f)
            putV2(this, 1f,  1f,  1f, 0f, 1f)
        }

        out.indices = createIndicesShort(6 * 6) {
            put(
                // front
                0,  1,  2,
                2,  3,  0,
                // top
                4,  5,  6,
                6,  7,  4,
                // back
                8,  9, 10,
                10, 11,  8,
                // bottom
                12, 13, 14,
                14, 15, 12,
                // left
                17, 16, 18,
                19, 18, 16,
                // right
                20, 21, 22,
                22, 23, 20
            )
        }

        return super.build(out)
    }

    private fun putV2(out: IFloatData, x: Float, y: Float, z: Float, u: Float, v: Float) {
        out.put(x * halfSizeX,  y * halfSizeY,  z * halfSizeZ)
        if (textureCoordinates) out.put(u * textureCoordinatesScale, v * textureCoordinatesScale)
        if (normals) out.put(normal.x, normal.y, normal.z)
    }
}
