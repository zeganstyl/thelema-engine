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

package app.thelema.gltf.tetf

import app.thelema.gltf.GLTFArrayElementAdapter
import app.thelema.gltf.GLTFTexture
import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO
import app.thelema.shader.node.GLSL
import app.thelema.shader.node.IShaderNode
import app.thelema.shader.node.TextureNode

/** @author zeganstyl */
class TETFShaderNode(
    val TETFShader: TETFShader,
    var node: IShaderNode? = null
): IJsonObjectIO, GLTFArrayElementAdapter(TETFShader.nodes) {
    override var name: String = ""
    var classId: String = ""

    /** (x, y) - position on shader flow diagram */
    var x: Float = 0f

    /** (x, y) - position on shader flow diagram */
    var y: Float = 0f

    override fun readJson(json: IJsonObject) {
        classId = json.string("classId", "")
        if (classId.isEmpty()) throw IllegalStateException("classId can't be empty")

        val newNode = GLSL.nodes[classId]!!.invoke()
        node = newNode

        if (classId == TextureNode.ClassId) {
            json.int("texture") { textureIndex ->
                newNode as TextureNode
                gltf.textures.getOrWaitTyped<GLTFTexture>(textureIndex) {
                    newNode.texture = it.texture
                }
            }
        }

        newNode.readJson(json)

        json.obj("outputs") {
            objs { key ->
                val connector = newNode.output[key] ?: error("Output \"$key\" is not found in node \"${newNode.name}\"")

                array("connectedTo") {
                    objs {
                        val nodeIndex = int("node")
                        TETFShader.nodes.getOrWaitTyped<TETFShaderNode>(nodeIndex) {
                            val inputName = string("input")
                            it.node!!.setInput(inputName, connector)
                        }
                    }
                }
            }
        }

        TETFShader.nodes.ready(elementIndex)
    }

    override fun writeJson(json: IJsonObject) {
        json["classId"] = classId

        val node = node
        if (node != null) {
            if (node.classId == TextureNode.ClassId) {
                node as TextureNode
                val texture = node.texture
                if (texture != null) {
                    val index = gltf.textures.indexOfFirst { (it as GLTFTexture).texture == texture }
                    if (index >= 0) json["texture"] = index
                }
            }

            node.writeJson(json)
            if (node.output.isNotEmpty()) {
                json.setObj("outputs") {
                    node.output.forEach { outputEntry ->
                        if (outputEntry.value.connectedTo.isNotEmpty()) {
                            setObj(outputEntry.key) {
                                setArray("connectedTo") {
                                    outputEntry.value.connectedTo.forEach { linkEntry ->
                                        addObj {
                                            val nodeIndex = TETFShader.nodes.indexOfFirst { (it as TETFShaderNode).node == linkEntry.node }
                                            set("node", nodeIndex)
                                            set("input", linkEntry.inputName)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun destroy() {}
}