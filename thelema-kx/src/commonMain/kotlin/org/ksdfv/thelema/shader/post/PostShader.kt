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

package org.ksdfv.thelema.shader.post

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.shader.Shader

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
        defaultPrecision = defaultPrecision,
        version = version
    )