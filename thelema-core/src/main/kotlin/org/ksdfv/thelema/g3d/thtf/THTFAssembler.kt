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

import org.ksdfv.thelema.g3d.gltf.GLTFAssembler
import org.ksdfv.thelema.g3d.gltf.IGLTF
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
class THTFAssembler(var thtf: ITHTF): GLTFAssembler(thtf) {
    override var gltf: IGLTF
        get() = thtf
        set(value) {
            thtf = value as ITHTF
        }

    fun addShader(shader: IShader) {
        val shaderIndex = thtf.shaders.size
        val thtfShader = THTFShader(thtf, shaderIndex)
        thtf.shaders.add(thtfShader)
        thtfShader.name = shader.name

        for (i in shader.nodes.indices) {
            val nodeIndex = shader.nodes.size
            val thtfNode = THTFShaderNode(thtfShader, nodeIndex)
            thtfShader.nodes.add(thtfNode)
            val node = shader.nodes[i]
            thtfNode.node = node
            thtfNode.classId = node.classId
        }
    }
}