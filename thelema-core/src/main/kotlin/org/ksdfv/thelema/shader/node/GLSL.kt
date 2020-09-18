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

package org.ksdfv.thelema.shader.node

import org.ksdfv.thelema.kx.ThreadLocal

/** Some common objects for shader nodes
 * @author zeganstyl */
@ThreadLocal
object GLSL {
    /** Registered nodes. Key - node class id, node builder function */
    val nodes = HashMap<String, () -> IShaderNode>()

    val zeroFloat = GLSLFloatInline(0f)
    val oneFloat = GLSLFloatInline(1f)

    val defaultNormal = GLSLVec3Inline(0.5f, 0.5f, 1f)

    init {
        nodes[VertexNode.ClassId] = { VertexNode() }
        nodes[CameraDataNode.ClassId] = { CameraDataNode() }
        nodes[UVNode.ClassId] = { UVNode() }
        nodes[OperationNode.ClassId] = { OperationNode() }
        nodes[OutputNode.ClassId] = { OutputNode() }
        nodes[TextureNode.ClassId] = { TextureNode() }
        nodes[NormalMapNode.ClassId] = { NormalMapNode() }
        nodes[PrincipledBSDF.ClassId] = { PrincipledBSDF() }
        nodes[ToneMapNode.ClassId] = { ToneMapNode() }
        nodes[GBufferOutputNode.ClassId] = { GBufferOutputNode() }
        nodes[SplitVec4Node.ClassId] = { SplitVec4Node() }
        nodes[MergeVec4.ClassId] = { MergeVec4() }
        nodes[AttributeNode.ClassId] = { AttributeNode() }
        nodes[SkyboxVertexNode.ClassId] = { SkyboxVertexNode() }
        nodes[VelocityNode.ClassId] = { VelocityNode() }
    }
}
