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

package org.ksdfv.thelema.g3d.thtf

import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.shader.node.GLSL
import org.ksdfv.thelema.shader.node.IShaderNode
import org.ksdfv.thelema.shader.node.TextureNode

/** @author zeganstyl */
class THTFShaderNode(
    val thtfShader: THTFShader,
    override var elementIndex: Int,
    override var name: String = "",
    var classId: String = "",
    var node: IShaderNode? = null
): IJsonObjectIO, ITHTFArrayElement {
    override val thtf: ITHTF
        get() = thtfShader.thtf

    /** (x, y) - position on shader flow diagram */
    var x: Float = 0f

    /** (x, y) - position on shader flow diagram */
    var y: Float = 0f

    override fun read(json: IJsonObject) {
        classId = json.string("classId", "")
        if (classId.isEmpty()) throw IllegalStateException("classId can't be empty")

        val newNode = GLSL.nodes[classId]!!.invoke()
        node = newNode

        if (classId == TextureNode.ClassId) {
            json.int("texture") { textureIndex ->
                newNode as TextureNode
                thtf.textures.getOrWait(textureIndex) {
                    newNode.texture = it.texture
                }
            }
        }

        newNode.read(json)

        json.obj("outputs") {
            objs { key ->
                val connector = newNode.output[key] ?: error("Output \"$key\" is not found in node \"${newNode.name}\"")

                array("connectedTo") {
                    objs {
                        val nodeIndex = int("node")
                        thtfShader.nodes.getOrWait(nodeIndex) {
                            val inputName = string("input")
                            it.node!!.setInput(inputName, connector)
                        }
                    }
                }
            }
        }

        thtfShader.nodes.ready(elementIndex)
    }

    override fun write(json: IJsonObject) {
        json["classId"] = classId

        val node = node
        if (node != null) {
            if (node.classId == TextureNode.ClassId) {
                node as TextureNode
                val texture = node.texture
                if (texture != null) {
                    val index = thtf.textures.indexOfFirst { it.texture == texture }
                    if (index >= 0) json["texture"] = index
                }
            }

            node.write(json)
            if (node.output.isNotEmpty()) {
                json.set("outputs") {
                    node.output.forEach { outputEntry ->
                        if (outputEntry.value.connectedTo.isNotEmpty()) {
                            set(outputEntry.key) {
                                setArray("connectedTo") {
                                    outputEntry.value.connectedTo.forEach { linkEntry ->
                                        addObj {
                                            val nodeIndex = thtfShader.nodes.indexOfFirst { it.node == linkEntry.node }
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