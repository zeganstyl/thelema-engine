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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.g3d.node.IDelegateTransformNode
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.shader.Shader

/** Visible transformable 3d object
 * @author zeganstyl */
interface IObject3D: IDelegateTransformNode {
    /** If previous transform data is not used, it must be linked to [ITransformNode.Cap] */
    var previousTransform: ITransformNode

    var isVisible: Boolean

    var meshes: MutableList<IMesh>

    var armature: IArmature?

    fun set(other: IObject3D): IObject3D {
        node.set(other)
        name = other.name
        isVisible = other.isVisible
        node = other.node
        meshes.addAll(other.meshes)
        armature = other.armature
        return this
    }

    override fun copy(): IObject3D {
        return this
    }

    override fun set(other: ITransformNode): IObject3D {
        super.set(other)
        return this
    }

    fun updatePreviousTransform() {
        if (previousTransform !== ITransformNode.Cap) {
            previousTransform.set(node)
        }
    }

    fun update(delta: Float) {}

    /** Render meshes with material shaders */
    fun render(shaderTypeId: Int = -1) {
        // Get all unique shaders and for preparing them once. HashSet will contain only unique shaders

        preparedShaders.clear()
        val meshes = meshes

        if (shaderTypeId == -1) {
            for (i in meshes.indices) {
                val mesh = meshes[i]
                val shader = mesh.material.shader
                if (shader != null) preparedShaders.add(shader)
            }

            preparedShaders.forEach { it.prepareObjectData(this) }

            for (i in meshes.indices) {
                val mesh = meshes[i]
                val shader = mesh.material.shader
                if (shader != null) mesh.render(shader)
            }
        } else {
            for (i in meshes.indices) {
                val mesh = meshes[i]
                val shader = mesh.material.shaderChannels[shaderTypeId] as Shader?
                if (shader != null) preparedShaders.add(shader)
            }

            preparedShaders.forEach { it.prepareObjectData(this) }

            for (i in meshes.indices) {
                val mesh = meshes[i]
                val shader = mesh.material.shaderChannels[shaderTypeId] as Shader?
                if (shader != null) mesh.render(shader)
            }
        }
    }

    override fun clear() {
        super.clear()
        meshes.clear()
        node = ITransformNode.Cap
        previousTransform = ITransformNode.Cap
        armature = null
    }

    companion object {
        /** Builder for internal creating */
        var Build: () -> IObject3D = { Object3D() }

        private val preparedShaders = HashSet<Shader>()
    }
}