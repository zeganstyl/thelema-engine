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

import app.thelema.app.APP
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.img.SimpleFrameBuffer
import app.thelema.gl.ScreenQuad
import app.thelema.img.render
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.post.PostShader
import app.thelema.test.Test
import app.thelema.utils.Color

/** @author zeganstyl */
class SobelBaseTest: Test {
    override val name: String
        get() = "Sobel filter"

    override fun testMain() {
        val box = BoxMesh { setSize(2f) }
        val boxShader = SimpleShader3D {
            setColor(Color.WHITE)
        }

        val frameBuffer = SimpleFrameBuffer()

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

        APP.onRender = {
            frameBuffer.render {
                box.render(boxShader)
            }

            frameBuffer.getTexture(0).bind(0)
            ScreenQuad.render(sobel)
        }
    }
}
