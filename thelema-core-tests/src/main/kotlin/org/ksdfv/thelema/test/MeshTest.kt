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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_UNSIGNED_SHORT
import org.ksdfv.thelema.mesh.*
import org.ksdfv.thelema.shader.Shader
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
object MeshTest: Test("Mesh") {
    override fun testMain() {
        val mesh = Mesh()

        mesh.vertices = IVertexBuffer.build(DATA.bytes(20 * 4).apply {
            floatView().apply {
                // x, y, z,   u, v
                put(-1f, -1f, 0f,   0f, 0f)
                put(1f, -1f, 0f,   1f, 0f)
                put(1f, 1f, 0f,   1f, 1f)
                put(-1f, 1f, 0f,   0f, 1f)
            }
        }, VertexAttributes(VertexAttribute.Position, VertexAttribute.UV[0]))

        mesh.indices = IndexBufferObject(DATA.bytes(12).apply {
            shortView().apply {
                put(0, 1, 2)
                put(0, 2, 3)
            }
        }, GL_UNSIGNED_SHORT)

        @Language("GLSL")
        val shader = Shader(
                vertCode = """          
attribute vec4 a_position;
attribute vec2 a_texCoord0;
varying vec2 v_texCoords;

void main() {
    v_texCoords = a_texCoord0;
    gl_Position = a_position;
}""",
                fragCode = """                
varying vec2 v_texCoords;

void main() {
    gl_FragColor = vec4(v_texCoords, 1, 1);
}""")

        println(shader.sourceCode())

        GL.render {
            shader.bind()
            mesh.render(shader)
        }
    }
}
