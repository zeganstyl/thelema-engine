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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.Blending
import org.ksdfv.thelema.g3d.light.ILight
import org.ksdfv.thelema.g3d.node.ITransformNode

/** @author zeganstyl */
interface IScene {
    var name: String

    val lights: MutableList<ILight>

    val objects: MutableList<IObject3D>

    val armatures: MutableList<IArmature>

    val nodes: MutableList<ITransformNode>

    val scenes: MutableList<IScene>

    var shaderChannel: Int

    /** Root node of this scene */
    var node: ITransformNode

    var world: IWorld

    /** Get lights from this scene and all children scenes recursively */
    fun getAllLights(out: MutableList<ILight>) {
        out.addAll(lights)

        var i = 0
        val childScenes = scenes
        while (i < childScenes.size) {
            childScenes[i].getAllLights(out)
            i++
        }
    }

    /** Get objects from this scene and all children scenes recursively.
     * Translucent is separate list, for reason that it may be sorted. */
    fun getAllObjects(opaque: MutableList<IObject3D>, translucent: MutableList<IObject3D>) {
        val objects = objects
        for (i in objects.indices) {
            val obj = objects[i]
            val meshes = obj.meshes
            var isTranslucent = false
            for (j in meshes.indices) {
                val mesh = meshes[j]
                if (mesh.material.alphaMode == Blending.Blend) {
                    isTranslucent = true
                    break
                }
            }
            if (isTranslucent) translucent.add(obj) else opaque.add(obj)
        }

        var i = 0
        val childScenes = scenes
        while (i < childScenes.size) {
            childScenes[i].getAllObjects(opaque, translucent)
            i++
        }
    }

    fun update(delta: Float = APP.deltaTime)

    fun render()

    fun copy(): IScene

    fun destroy() {
        objects.clear()
    }
}