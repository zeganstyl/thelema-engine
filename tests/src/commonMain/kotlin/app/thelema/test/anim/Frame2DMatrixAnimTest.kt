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

package app.thelema.test.anim

import app.thelema.anim.Frame2DAnim
import app.thelema.anim.Frame2DAnimConf
import app.thelema.app.APP
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.Texture2D
import app.thelema.math.Mat3
import app.thelema.gl.ScreenQuad
import app.thelema.shader.post.PostShader
import app.thelema.test.Test

class Frame2DMatrixAnimTest: Test {
    override val name: String
        get() = "Frame 2D animation"

    override fun testMain() {
        val shader = PostShader(fragCode = """
varying vec2 uv;

uniform sampler2D tex;
uniform mat3 animMatrix;

void main() {
    vec3 uvAnimated = animMatrix * vec3(uv.x, 1.0 - uv.y, 1.0); // Y is flipped
    gl_FragColor = texture2D(tex, uvAnimated.xy);
}
""")
        shader["tex"] = 0

        val animConf = Frame2DAnimConf(4, 4)
        animConf.loop = true
        animConf.speed = 10f
        val anim = Frame2DAnim(animConf)

        val animMatrix = Mat3()

        val tex = Texture2D("Hit-Yellow.png")

        APP.onRender = {
            anim.update(APP.deltaTime)

            anim.setTo(animMatrix)

            tex.bind(0)
            shader.bind()
            shader["animMatrix"] = animMatrix

            ScreenQuad.render(shader)
        }
    }
}