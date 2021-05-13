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

import app.thelema.ecs.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.node.ITransformNode
import app.thelema.g3d.node.TransformNode
import app.thelema.gl.IMesh
import app.thelema.json.IJsonObject
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.res.onComponentAdded
import app.thelema.shader.IShader

/** @author zeganstyl */
class Object3D: IObject3D {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
            value?.getComponentOrNull<IMesh>()?.also { addMesh(it) }
        }

    override var node: ITransformNode = TransformNode()

    private val meshesInternal = ArrayList<IMesh?>()
    override val meshes: MutableList<IMesh?>
        get() = meshesInternal

    private val opaqueMeshes = ArrayList<IMesh>()
    private val translucentMeshes = ArrayList<IMesh>()

    private val tmp = Vec3()

    var meshesSorter: Comparator<IMesh> = Comparator { o1, o2 ->
        var result = 0
        val mesh1Priority = meshesInternal.maxByOrNull { it?.material?.translucentPriority ?: Int.MIN_VALUE }?.material?.translucentPriority
        val mesh2Priority = meshesInternal.maxByOrNull { it?.material?.translucentPriority ?: Int.MIN_VALUE }?.material?.translucentPriority
        if (mesh1Priority != null && mesh2Priority != null) {
            result = when {
                mesh1Priority > mesh2Priority -> 1
                mesh1Priority < mesh2Priority -> -1
                else -> {
                    val dst = ((1000f * ActiveCamera.node.getGlobalPosition(tmp).dst2(o1.centroid)).toInt() - (1000f * ActiveCamera.node.getGlobalPosition(tmp).dst2(o2.centroid)).toInt()).toFloat()
                    if (dst < 0) -1 else if (dst > 0) 1 else 0
                }
            }
        }
        result
    }

    private val preparedShaders = HashSet<IShader>()

    override var isVisible: Boolean = true
    override var armature: IArmature? = null
    override var boundingBox: IBoundingBox? = null

    override val componentName: String
        get() = Name

    private var alphaModeInternal = Blending.OPAQUE
    override val alphaMode: String
        get() = alphaModeInternal

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMesh) addMesh(component)
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)

        meshesInternal.clear()
        json.array("meshes") {
            for (i in 0 until size) {
                meshesInternal.add(null)
            }
            meshesInternal.trimToSize()

            for (i in 0 until size) {
                RES.onComponentAdded<IMesh>(string(i)) { setMesh(i, it) }
            }
        }
    }

    override fun writeJson(json: IJsonObject) {
        super.writeJson(json)

        json.setArray("meshes") {
            for (i in meshes.indices) {
                val mesh = meshes[i]
                if (mesh != null) add(mesh.entity.path)
            }
        }
    }

    override fun addMesh(mesh: IMesh?) {
        meshesInternal.add(mesh)
        if (mesh?.material?.alphaMode == Blending.BLEND) alphaModeInternal = Blending.BLEND
    }

    override fun removeMesh(mesh: IMesh) {
        meshesInternal.remove(mesh)
    }

    override fun setMesh(index: Int, mesh: IMesh?) {
        meshesInternal[index] = mesh
        if (mesh?.material?.alphaMode == Blending.BLEND) alphaModeInternal = Blending.BLEND
    }

    override fun removeMesh(index: Int) {
        meshesInternal.removeAt(index)
    }

    fun validate() {
        alphaModeInternal = Blending.OPAQUE
        for (i in meshesInternal.indices) {
            val mesh = meshesInternal[i]
            if (mesh?.material?.alphaMode == Blending.BLEND) {
                alphaModeInternal = Blending.BLEND
            }
        }
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        super.setComponent(other)

        if (other is IObject3D && other != this) {
            isVisible = other.isVisible
            meshesInternal.clear()
            meshesInternal.addAll(other.meshes)

            val otherArmature = other.armature
            armature = if (otherArmature != null) {
                val path = other.entity.getRelativePathTo(otherArmature.entity)
                entity.getEntityByPath(path)?.getComponentOrNull()
            } else {
                null
            }
        }

        return this
    }

    override fun updatePreviousTransform() {
    }

    override fun render(shaderChannel: String?) {
        // Get all unique shaders and for preparing them once. HashSet will contain only unique shaders

        preparedShaders.clear()

        opaqueMeshes.clear()
        translucentMeshes.clear()
        for (i in meshes.indices) {
            val mesh = meshes[i]
            if (mesh != null) {
                if (mesh.material?.alphaMode == Blending.BLEND) {
                    translucentMeshes.add(mesh)
                } else {
                    opaqueMeshes.add(mesh)
                }
            }
        }

        translucentMeshes.sortedWith(meshesSorter)

        for (i in meshes.indices) {
            val material = meshes[i]?.material
            if (material != null) {
                val shader = if (shaderChannel == null) material.shader else material.shaderChannels[shaderChannel]
                if (shader != null) preparedShaders.add(shader)
            }
        }

        preparedShaders.forEach { it.prepareObjectData(this) }

        for (i in opaqueMeshes.indices) {
            val mesh = opaqueMeshes[i]
            val material = mesh.material
            if (material != null) {
                val shader = if (shaderChannel == null) material.shader else material.shaderChannels[shaderChannel]
                if (shader != null) mesh.render(shader)
            }
        }

        for (i in translucentMeshes.indices) {
            val mesh = translucentMeshes[i]
            val material = mesh.material
            if (material != null) {
                val shader = if (shaderChannel == null) material.shader else material.shaderChannels[shaderChannel]
                if (shader != null) mesh.render(shader)
            }
        }
    }

    companion object {
        const val Name = "Object3D"
    }
}
