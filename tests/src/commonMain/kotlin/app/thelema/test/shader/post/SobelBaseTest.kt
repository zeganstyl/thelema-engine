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

package app.thelema.test.shader.post


import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_RGB
import app.thelema.img.SimpleFrameBuffer
import app.thelema.gl.ScreenQuad
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.post.PostShader
import app.thelema.test.CubeModel
import app.thelema.test.Test

/** @author zeganstyl */
class SobelBaseTest: Test {
    override val name: String
        get() = "Sobel filter"

    override fun testMain() {
        val model = CubeModel(
            Shader(
                vertCode = """            
attribute vec3 POSITION;
varying vec3 vPosition;
uniform mat4 projViewModelTrans;

void main() {
    vPosition = POSITION.xyz;
    gl_Position = projViewModelTrans * vec4(POSITION, 1.0);
}""",
                fragCode = """
varying vec3 vPosition;
void main() {
    gl_FragColor = vec4(1.0);
}""")
        )

        val frameBuffer = SimpleFrameBuffer(
            width = GL.mainFrameBufferWidth,
            height = GL.mainFrameBufferHeight,
            pixelFormat = GL_RGB
        )

        val sobel = PostShader(
            fragCode = """
// Sobel Edge Detection Filter
// GLSL Fragment Shader
// Implementation by Patrick Hebron

uniform sampler2D	tex;
uniform float 		width;
uniform float 		height;
varying vec2 uv;

void main(void) 
{
	vec4 n[9];

    float w = 1.0 / width;
	float h = 1.0 / height;

	n[0] = texture2D(tex, uv + vec2( -w, -h));
	n[1] = texture2D(tex, uv + vec2(0.0, -h));
	n[2] = texture2D(tex, uv + vec2(  w, -h));
	n[3] = texture2D(tex, uv + vec2( -w, 0.0));
	n[4] = texture2D(tex, uv);
	n[5] = texture2D(tex, uv + vec2(  w, 0.0));
	n[6] = texture2D(tex, uv + vec2( -w, h));
	n[7] = texture2D(tex, uv + vec2(0.0, h));
	n[8] = texture2D(tex, uv + vec2(  w, h));

	vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
  	vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
	vec4 sobel = (sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v);
    gl_FragColor = vec4(sobel.xyz, 1.0);
}"""
        )

        sobel.bind()
        sobel["tex"] = 0
        sobel["width"] = frameBuffer.width.toFloat()
        sobel["height"] = frameBuffer.height.toFloat()

        val screenQuad = ScreenQuad()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            frameBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.update()
                model.render()
            }

            frameBuffer.getTexture(0).bind(0)
            screenQuad.render(sobel)
        }
    }
}
