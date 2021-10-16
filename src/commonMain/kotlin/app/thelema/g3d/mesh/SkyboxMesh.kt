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

class SkyboxMesh(): MeshBuilderAdapter() {
    constructor(block: SkyboxMesh.() -> Unit): this() {
        block(this)
    }

    override val componentName: String
        get() = "SkyboxMesh"

    override var builder: MeshBuilder
        get() = super.builder
        set(value) {
            super.builder = value
            value.uvs = false
            value.normals = false
            value.tangents = false
        }

    init {
        getOrCreateEntity()
        updateMesh()
    }

    override fun getVerticesCount(): Int = 8

    override fun getIndicesCount(): Int = 36

    override fun applyVertices() {
        preparePositions {
            val r = 0.5f
            setFloats(
                // front
                -r, -r,  r,
                r, -r,  r,
                r,  r,  r,
                -r,  r,  r,
                // back
                -r, -r, -r,
                r, -r, -r,
                r,  r, -r,
                -r,  r, -r
            )
        }
    }

    override fun applyIndices() {
        prepareIndices {
            putIndices(
                // front
                2, 1, 0,
                0, 3, 2,
                // right
                6, 5, 1,
                1, 2, 6,
                // back
                5, 6, 7,
                7, 4, 5,
                // left
                3, 0, 4,
                4, 7, 3,
                // bottom
                1, 5, 4,
                4, 0, 1,
                // top
                6, 2, 3,
                3, 7, 6
            )
        }
    }
}

fun IEntity.skyboxMesh(block: SkyboxMesh.() -> Unit) = component(block)
fun IEntity.skyboxMesh() = component<SkyboxMesh>()