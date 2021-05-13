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

package app.thelema.g3d

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.node.ITransformData
import app.thelema.g3d.node.ITransformNode
import app.thelema.gl.IMesh

/** Visible transformable 3d object
 * @author zeganstyl */
interface IObject3D: IEntityComponent {
    val node: ITransformNode

    var isVisible: Boolean

    val meshes: List<IMesh?>

    var armature: IArmature?

    var boundingBox: IBoundingBox?

    val alphaMode: String

    fun addMesh(mesh: IMesh?)

    fun setMesh(index: Int, mesh: IMesh?)

    fun removeMesh(mesh: IMesh)

    fun removeMesh(index: Int)

    fun updatePreviousTransform()

    /** Render meshes with material shaders */
    fun render(shaderChannel: String? = null)
}