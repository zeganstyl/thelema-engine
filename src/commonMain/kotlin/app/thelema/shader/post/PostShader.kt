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

package app.thelema.shader.post

import app.thelema.app.APP
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.IFrameBuffer
import app.thelema.gl.IMesh
import app.thelema.img.render
import app.thelema.shader.Shader

/** @author zeganstyl */
open class PostShader(
    fragCode: String,
    name: String = "",
    compile: Boolean = true,
    defaultPrecision: String = if (APP.platformType != APP.Desktop) "precision mediump float;\n" else "",
    uvName: String = "uv",
    attributeUVName: String = "UV",
    attributePositionName: String = "POSITION",
    version: Int = 110
) :
    Shader(
        vertCode = """
attribute vec2 $attributePositionName;
attribute vec2 $attributeUVName;
varying vec2 $uvName;

void main() {
    $uvName = $attributeUVName;
    gl_Position = vec4($attributePositionName, 0.0, 1.0);
}""",
        fragCode = fragCode,
        name = name,
        compile = compile,
        floatPrecision = defaultPrecision,
        version = version
    )
{
    protected open fun renderScreenQuad(screenQuad: IMesh, out: IFrameBuffer?) {
        if (out != null) {
            out.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                screenQuad.render(this@PostShader)
            }
        } else {
            screenQuad.render(this@PostShader)
        }
    }
}