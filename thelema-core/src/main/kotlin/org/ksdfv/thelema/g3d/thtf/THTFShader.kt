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

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.shader.IShader
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.utils.AsyncArrayList
import org.ksdfv.thelema.utils.IAsyncList

/** @author zeganstyl */
class THTFShader(
    override val thtf: ITHTF,
    override var elementIndex: Int,
    var shader: IShader? = null,
    override var name: String = ""
): ITHTFArrayElement {
    val nodes: IAsyncList<THTFShaderNode> = AsyncArrayList()

    override fun read(json: IJsonObject) {
        name = json.string("name", "")

        json.array("nodes") {
            objs {
                val node = THTFShaderNode(this@THTFShader, nodes.size)
                nodes.add(node)
                node.read(this)
            }
        }

        val newShader = Shader()
        nodes.forEach { newShader.addNode(it.node!!) }
        shader = newShader

        if (thtf.conf.separateThread) {
            GL.call {
                newShader.build()
            }
        } else {
            newShader.build()
        }

        thtf.shaders.ready(elementIndex)
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name

        if (nodes.isNotEmpty()) {
            json.setArray("nodes") {
                for (i in nodes.indices) {
                    add(nodes[i])
                }
            }
        }
    }

    fun assembleFrom(shader: IShader) {
        this.shader = shader
        name = shader.name

        for (i in shader.nodes.indices) {
            val nodeIndex = shader.nodes.size
            val thtfNode = THTFShaderNode(thtfShader = this, elementIndex = nodeIndex)
            nodes.add(thtfNode)
            val node = shader.nodes[i]
            thtfNode.node = node
            thtfNode.classId = node.classId
        }
    }

    override fun destroy() {}
}