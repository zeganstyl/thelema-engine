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


import app.thelema.app.APP
import app.thelema.gl.GL
import app.thelema.gl.ScreenQuad
import app.thelema.shader.Shader
import app.thelema.shader.post.PostShader
import app.thelema.test.Test

/** @author zeganstyl */
class ScreenQuadTest: Test {
    override fun testMain() {
        val shader = PostShader(
                fragCode = """
in vec2 uv;
out vec4 FragColor;

void main() {
    FragColor = vec4(uv, 1.0, 1.0);
}
""")

        APP.onRender = {
            ScreenQuad.render(shader)
        }
    }
}
