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

package app.thelema.test

import app.thelema.shader.Shader

/** Used to render simple meshes in tests.
 * @param colorName if not null, will be used as fragment shader output color, otherwise will be used UVs */
class SimpleMeshShader(
    val positionName: String = "POSITION",
    val uvName: String = "UV",
    val viewProjName: String = "viewProj",
    colorName: String? = null
): Shader(
    vertCode = """
attribute vec3 $positionName;
attribute vec2 $uvName;
varying vec2 uv;
uniform mat4 $viewProjName;

void main() {
    uv = $uvName;
    gl_Position = $viewProjName * vec4($positionName, 1.0);
}""",
    fragCode = """
varying vec2 uv;
${if (colorName != null) "uniform vec4 $colorName;" else ""}

void main() {
    gl_FragColor = ${colorName ?: "vec4(uv, 0.0, 1.0)"};
}""") {

    /** Use given color to render all meshes with it */
    constructor(
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1f,
        positionName: String = "POSITION",
        uvName: String = "UV",
        projViewName: String = "projView"
    ): this(positionName, uvName, projViewName, "color") {
        bind()
        set("color", r, g, b, a)
    }
}