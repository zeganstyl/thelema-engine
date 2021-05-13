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
import app.thelema.g3d.light.ILight
import app.thelema.g3d.node.ITransformNode
import app.thelema.gl.GL
import app.thelema.math.Vec3
import app.thelema.shader.IShader

/** @author zeganstyl */
class Scene: IScene {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            value?.component<ITransformNode>()
            if (value != null) addedEntityToBranch(value)
        }

    override val componentName: String
        get() = Name

    override val lights: MutableList<ILight> = ArrayList()
    override val objects: MutableList<IObject3D> = ArrayList()

    override var world: IWorld? = null

    private val tmp = Vec3()

    var objectSorter: Comparator<IObject3D> = Comparator { o1, o2 ->
        var result = 0
        o1.node.getGlobalPosition(tmpV1)
        o2.node.getGlobalPosition(tmpV2)
        val mesh1Priority = o1.meshes.maxByOrNull { it?.material?.translucentPriority ?: Int.MIN_VALUE }?.material?.translucentPriority
        val mesh2Priority = o2.meshes.maxByOrNull { it?.material?.translucentPriority ?: Int.MIN_VALUE }?.material?.translucentPriority
        if (mesh1Priority != null && mesh2Priority != null) {
            result = when {
                mesh1Priority > mesh2Priority -> 1
                mesh1Priority < mesh2Priority -> -1
                else -> {
                    val dst = ((1000f * ActiveCamera.node.getGlobalPosition(tmp).dst2(tmpV1)).toInt() - (1000f * ActiveCamera.node.getGlobalPosition(tmp).dst2(tmpV2)).toInt()).toFloat()
                    if (dst < 0) -1 else if (dst > 0) 1 else 0
                }
            }
        }
        result
    }

    private val tmpV1 = Vec3()
    private val tmpV2 = Vec3()

    private val opaque = ArrayList<IObject3D>()
    private val translucent = ArrayList<IObject3D>()

    private val shadersToUpdate = HashSet<IShader>()

    override fun add(obj: IObject3D) { objects.add(obj) }
    override fun add(light: ILight) { lights.add(light) }

//    override fun set(other: IEntityComponent): IEntityComponent {
//        //newScene.world = world
//        other as IScene
//
//        other.relatedNodes.forEach {
//            val path = other.entity.getRelativePathTo((it as IEntityComponent).entity)
//            val node = other.entity.getEntityByPath(path)?.getComponentOrNull("TransformNode")
//            if (node != null) {
//                relatedNodes.add(node as ITransformNode)
//            } else {
//                LOG.error("Scene: can't link node ${it.name} by path: $path")
//            }
//        }
//
//        return this
//    }

    override fun addedComponentToBranch(component: IEntityComponent) = addedComponent(component)

    private fun addedComponent(component: IEntityComponent) {
        if (component is ILight) lights.add(component)
        if (component is IObject3D) objects.add(component)
    }

    override fun addedSiblingComponent(component: IEntityComponent) = addedComponent(component)

    override fun updatePreviousTransform() {
        objects.forEach {
            it.updatePreviousTransform()
            it.armature?.updatePreviousTransform()
        }
    }

    override fun update(delta: Float) {}

    private fun getShadersToPrepare(objects: List<IObject3D>, shaderChannel: String?) {
        for (i in objects.indices) {
            val obj = objects[i]
            for (j in obj.meshes.indices) {
                val material = obj.meshes[j]?.material
                if (material != null) {
                    val shader = if (shaderChannel == null) material.shader else material.shaderChannels[shaderChannel]
                    if (shader != null) shadersToUpdate.add(shader)
                }
            }
        }
    }

    override fun render(shaderChannel: String?) {
        // Get all unique shaders
        shadersToUpdate.clear()

        opaque.clear()
        translucent.clear()

        val objects = objects
        for (i in objects.indices) {
            val obj = objects[i]
            if (obj.alphaMode == Blending.BLEND) translucent.add(obj) else opaque.add(obj)
        }

        getShadersToPrepare(opaque, shaderChannel)
        getShadersToPrepare(translucent, shaderChannel)
        shadersToUpdate.forEach { it.prepareSceneData(this) }

        for (i in opaque.indices) {
            GL.resetTextureUnitCounter()
            opaque[i].render(shaderChannel)
        }

        if (translucent.size > 0) {
            translucent.sortWith(objectSorter)

            for (i in 0 until translucent.size) {
                GL.resetTextureUnitCounter()
                translucent[i].render(shaderChannel)
            }
        }
    }

    companion object {
        const val Name = "Scene"
    }
}