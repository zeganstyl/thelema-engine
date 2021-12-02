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

package app.thelema.shader.node

import kotlin.native.concurrent.ThreadLocal

/** Some common objects for shader nodes
 * @author zeganstyl */
@ThreadLocal
object GLSL {
    val undefined = GLSLFloatInline(0f)
    val zeroFloat = GLSLFloatInline(0f)
    val oneFloat = GLSLFloatInline(1f)

    val defaultNormal = GLSLVec3Inline(0.5f, 0.5f, 1f)

    private var idCounter = 0L

    fun id() = idCounter++

    fun resetIdCounter() {
        idCounter = 0
    }
}

object GLSLNode {
    val vertex = VertexNode()

    val camera = CameraDataNode(vertex.position)

    val uv = UVNode()
}