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

import org.ksdfv.thelema.g3d.light.ILight
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.g3d.node.Node
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.shader.IShader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/** @author zeganstyl */
open class Scene(
    override var name: String = "",
    override var node: ITransformNode = Node()
): IScene {
    override var lights: MutableList<ILight> = ArrayList()
    override var objects: MutableList<IObject3D> = ArrayList()
    override var armatures: MutableList<IArmature> = ArrayList()
    override var nodes: MutableList<ITransformNode> = ArrayList()
    override var scenes: MutableList<IScene> = ArrayList()

    override var world: IWorld = IWorld.Default

    var objectSorter: Comparator<IObject3D> = Comparator { o1, o2 ->
        var result = 0
        o1.node.getPosition(tmpV1, false)
        o2.node.getPosition(tmpV2, false)
        val mesh1Priority = o1.meshes.maxBy { it.material.translucentPriority }?.material?.translucentPriority
        val mesh2Priority = o2.meshes.maxBy { it.material.translucentPriority }?.material?.translucentPriority
        if (mesh1Priority != null && mesh2Priority != null) {
            result = when {
                mesh1Priority > mesh2Priority -> 1
                mesh1Priority < mesh2Priority -> -1
                else -> {
                    val dst = ((1000f * ActiveCamera.dst2(tmpV1)).toInt() - (1000f * ActiveCamera.dst2(tmpV2)).toInt()).toFloat()
                    if (dst < 0) -1 else if (dst > 0) 1 else 0
                }
            }
        }
        result
    }

    override var shaderChannel: Int = -1

    override fun updatePreviousTransform() {
        objects.forEach {
            it.updatePreviousTransform()
            it.armature?.updatePreviousTransform()
        }

        scenes.forEach { it.updatePreviousTransform() }
    }

    override fun update(delta: Float) {
        nodes.forEach { it.updateTransform() }

        val objects = objects

        objectsToUpdate.clear()
        armaturesToUpdate.clear()
        for (i in objects.indices) {
            val obj = objects[i]
            objectsToUpdate.add(obj)
            val armature = obj.armature
            if (armature != null) armaturesToUpdate.add(armature)
        }
        objectsToUpdate.forEach { it.update(delta) }
        armaturesToUpdate.forEach { it.update(delta) }

        scenes.forEach { it.update(delta) }
    }

    private fun prepareShaders(objects: List<IObject3D>) {
        val shaderChannel = shaderChannel
        if (shaderChannel == -1) {
            for (i in objects.indices) {
                val obj = objects[i]
                val meshes = obj.meshes
                for (j in 0 until meshes.size) {
                    val shader = meshes[j].material.shader
                    if (shader != null) shadersToUpdate.add(shader)
                }
            }
        } else {
            for (i in objects.indices) {
                val obj = objects[i]
                val meshes = obj.meshes
                for (j in 0 until meshes.size) {
                    val shader = meshes[j].material.shaderChannels[shaderChannel]
                    if (shader != null) shadersToUpdate.add(shader)
                }
            }
        }
        shadersToUpdate.forEach { it.prepareSceneData(this) }
    }

    override fun render() {
        // Get all unique shaders
        shadersToUpdate.clear()

        opaque.clear()
        translucent.clear()
        getAllObjects(opaque, translucent)

        prepareShaders(opaque)
        prepareShaders(translucent)

        for (i in opaque.indices) {
            opaque[i].render(shaderChannel)
        }

        if (translucent.size > 0) {
            translucent.sortedWith(objectSorter)

            for (i in 0 until translucent.size) {
                translucent[i].render(shaderChannel)
            }
        }
    }

    override fun copy(): Scene {
        val newScene = Scene()

        newScene.name = name
        newScene.world = world
        newScene.shaderChannel = shaderChannel

        val nodes = nodes
        val newNodes = newScene.nodes

        // copy nodes
        for (i in nodes.indices) {
            newNodes.add(nodes[i].copy())
        }

        // link nodes
        for (i in nodes.indices) {
            val childNodes = nodes[i].childNodes
            for (j in childNodes.indices) {
                newNodes[i].addChildNode(newNodes[nodes.indexOf(childNodes[j])])
            }
        }

        // copy armatures
        val armatures = armatures
        val newArmatures = newScene.armatures
        for (i in armatures.indices) {
            val newArmature = armatures[i].copy()
            newArmature.linkBones(newNodes)
            newArmatures.add(newArmature)
        }

        // copy objects
        val objects = objects
        val newObjects = newScene.objects
        for (i in objects.indices) {
            val obj = objects[i]
            val newObj = obj.copy()

            newObj.node = newNodes[nodes.indexOf(obj.node)]

            val armature = obj.armature
            if (armature != null) {
                newObj.armature = newArmatures.getOrNull(armatures.indexOf(armature))
            }

            newObjects.add(newObj)
        }

        // copy lights
        val lights = lights
        val newLights = newScene.lights
        for (i in lights.indices) {
            newLights.add(lights[i].copy())
        }

        return newScene
    }

    companion object {
        val tmpV1 = Vec3()
        val tmpV2 = Vec3()

        private val objectsToUpdate = HashSet<IObject3D>()
        private val armaturesToUpdate = HashSet<IArmature>()
        private val shadersToUpdate = HashSet<IShader>()

        private val opaque = ArrayList<IObject3D>()
        private val translucent = ArrayList<IObject3D>()
    }
}