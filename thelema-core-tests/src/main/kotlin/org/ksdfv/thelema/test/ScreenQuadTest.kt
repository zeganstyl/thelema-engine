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

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.shader.Shader
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
object ScreenQuadTest: Test("Screen Quad") {
    override fun testMain() {
        @Language("GLSL")
        val shader = Shader(
                vertCode = Shader.RenderTextureVertexShader,
                fragCode = """
varying vec2 v_texCoords;

void main() {
    gl_FragColor = vec4(v_texCoords, 1.0, 1.0);
}""")

        println(shader.sourceCode())

        val screenQuad = ScreenQuad()

        GL.render {
            shader.bind()
            screenQuad.render(shader)
        }
    }
}
