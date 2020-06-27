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

import org.ksdfv.thelema.mesh.IMesh
import kotlin.math.sqrt

class SkyBoxMeshBuilder: MeshBuilder() {
    override var textureCoordinates: Boolean
        get() = false
        set(_) {}

    override var normals: Boolean
        get() = false
        set(_) {}

    override var textureCoordinatesScale: Float
        get() = 0f
        set(_) {}

    override fun build(out: IMesh): IMesh {
        out.vertices = createVerticesFloat(24) {
            val scale = 1f / sqrt(3f)
            put(
                -scale, -scale,  scale,
                scale, -scale,  scale,
                scale,  scale,  scale,
                -scale,  scale,  scale,

                -scale, -scale, -scale,
                scale, -scale, -scale,
                scale,  scale, -scale,
                -scale,  scale, -scale
            )
        }

        out.indices = createIndicesShort(6 * 6) {
            put(
                // front
                0, 1, 2,
                2, 3, 0,
                // right
                1, 5, 6,
                6, 2, 1,
                // back
                7, 6, 5,
                5, 4, 7,
                // left
                4, 0, 3,
                3, 7, 4,
                // bottom
                4, 5, 1,
                1, 0, 4,
                // top
                3, 2, 6,
                6, 7, 3
            )
        }

        return super.build(out)
    }
}