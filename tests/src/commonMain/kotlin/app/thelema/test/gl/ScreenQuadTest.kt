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

package app.thelema.test.gl


import app.thelema.gl.GL
import app.thelema.gl.ScreenQuad
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class ScreenQuadTest: Test {
    override val name: String
        get() = "Screen Quad"

    override fun testMain() {

        val shader = Shader(
                vertCode = """
attribute vec2 POSITION;
attribute vec2 UV;
varying vec2 uv;

void main() {
    uv = UV;
    gl_Position = vec4(POSITION, 0.0, 1.0);
}""",
                fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 1.0, 1.0);
}""")

        val screenQuad = ScreenQuad(positionName = "POSITION", uvName = "UV")

        GL.render {
            shader.bind()
            screenQuad.render(shader)
        }
    }
}
