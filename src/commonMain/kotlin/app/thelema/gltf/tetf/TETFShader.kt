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

import app.thelema.gltf.GLTFArray
import app.thelema.gltf.GLTFArrayElementAdapter
import app.thelema.gltf.IGLTFArray
import app.thelema.shader.IShader
import app.thelema.shader.Shader

/** @author zeganstyl */
class TETFShader(
    array: IGLTFArray,
    var shader: IShader? = null
): GLTFArrayElementAdapter(array) {
    val nodes = GLTFArray("nodes", array.gltf) { TETFShaderNode(this) }

    override fun readJson() {
        super.readJson()

        json.array("nodes") {
            objs {
                nodes.addElement(this)
            }
        }

        val newShader = Shader()
        nodes.forEach { newShader.addNode((it as TETFShaderNode).node!!) }
        shader = newShader

        gltf.runGLCall { newShader.build() }

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        if (nodes.isNotEmpty()) {
            json.setArray("nodes") {
                for (i in nodes.indices) {
                    add(nodes[i].json)
                }
            }
        }
    }

    fun assembleFrom(shader: IShader) {
        this.shader = shader
        name = shader.name

        for (i in shader.nodes.indices) {
            val thtfNode = TETFShaderNode(this)
            nodes.add(thtfNode)
            val node = shader.nodes[i]
            thtfNode.node = node
            thtfNode.classId = node.classId
        }
    }

    override fun destroy() {}
}