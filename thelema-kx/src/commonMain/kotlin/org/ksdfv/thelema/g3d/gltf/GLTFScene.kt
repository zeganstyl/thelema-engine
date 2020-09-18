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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class GLTFScene(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var scene: IScene = Scene()
): IJsonObjectIO, IGLTFArrayElement {
    override var name: String = ""
    val nodes: MutableList<Int> = ArrayList()

    override fun read(json: IJsonObject) {
        json.string("name") { name = it }

        scene.objects.addAll(gltf.objects)
        gltf.skins.forEach { scene.armatures.add(it.skin) }

        nodes.clear()
        json.array("nodes") {
            val sceneNodes = scene.nodes
            ints { nodeIndex ->
                nodes.add(nodeIndex)
                gltf.nodes.getOrWait(nodeIndex) { gltfNode ->
                    sceneNodes.add(gltfNode.node)
                    scene.node.addChildNode(gltfNode.node)
                }
            }
        }
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name
        if (nodes.isNotEmpty()) json.setInts("nodes", nodes.size) { nodes[it] }
    }

    override fun destroy() {
        scene.destroy()
    }
}