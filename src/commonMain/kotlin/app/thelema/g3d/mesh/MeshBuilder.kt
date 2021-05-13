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
import app.thelema.gl.GL_UNSIGNED_SHORT
import app.thelema.gl.IMesh
import app.thelema.gl.Mesh
import app.thelema.gl.mapFloat
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.math.mul

class MeshBuilder: IEntityComponent {
    override val componentName: String
        get() = "MeshBuilder"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            meshInternal = value?.component() ?: Mesh()
        }

    var uvs = true
    var normals = true

    var positionName: String = "POSITION"
    var uvName: String = "UV"
    var normalName: String = "NORMAL"

    var indexType: Int = GL_UNSIGNED_SHORT

    var meshInternal: IMesh = Mesh()
    val mesh: IMesh
        get() = meshInternal

    val position = Vec3(0f, 0f, 0f)
    val rotation = Vec4(0f, 0f, 0f, 1f)
    val scale = Vec3(1f, 1f, 1f)

    /** Apply transformation (position, rotation, scale) */
    fun applyTransform() {
        if (rotation.isNotIdentity) {
            val mat = Mat4().set(position, rotation, scale)
            mesh.getAttribute(positionName) {
                rewind()
                for (i in 0 until buffer.verticesCount) {
                    mat.mul(getFloat(0), getFloat(4), getFloat(8)) { x, y, z ->
                        putFloat(0, x)
                        putFloat(4, y)
                        putFloat(8, z)
                    }
                    nextVertex()
                }
                rewind()
            }
        } else {
            if (position.isNotZero) {
                mesh.getAttribute(positionName) {
                    rewind()
                    for (i in 0 until buffer.verticesCount) {
                        mapFloat(0) { it + position.x }
                        mapFloat(4) { it + position.y }
                        mapFloat(8) { it + position.z }
                        nextVertex()
                    }
                    rewind()
                }
            }

            if (scale.isNotEqual(1f, 1f, 1f)) {
                mesh.getAttribute(positionName) {
                    rewind()
                    for (i in 0 until buffer.verticesCount) {
                        mapFloat(0) { it * scale.x }
                        mapFloat(4) { it * scale.y }
                        mapFloat(8) { it * scale.z }
                        nextVertex()
                    }
                    rewind()
                }
            }
        }
    }
}