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

package app.thelema.shader

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.IMesh
import app.thelema.img.ITexture2D
import app.thelema.math.IMat4
import app.thelema.math.IVec4

/** Can be used for debugging purpose */
class SimpleShader3D(block: SimpleShader3D.() -> Unit = {}): Shader() {
    var positionName: String = "POSITION"
    var uvName: String = "UV"

    var renderAttributeName: String = uvName

    var color: IVec4? = null

    var multiplyColor = false

    var worldMatrix: IMat4? = null

    var colorTexture: ITexture2D? = null

    private var drawing = false

    init {
        block(this)
        initShader()

        onMeshDraw = { render(it) }
    }

    fun initShader() {
        vertCode = """
attribute vec3 $positionName;
${if (uvName.isNotEmpty()) "attribute vec2 $uvName;" else "" }

varying vec3 pos;
varying vec2 uv;
uniform mat4 viewProj;
${if (worldMatrix != null) "uniform mat4 worldMatrix;" else ""}

void main() {
    pos = $positionName;
    ${if (uvName.isNotEmpty()) "uv = $uvName;" else "uv = vec2(1.0);" }
    gl_Position = viewProj ${if (worldMatrix != null) "* worldMatrix " else "" }* vec4($positionName, 1.0);
}
"""
        fragCode = """
varying vec3 pos;
varying vec2 uv;

${if (color != null) "uniform vec4 color;" else "" }
${if (colorTexture != null) "uniform sampler2D tex;" else ""}

void main() {
    ${
        if (colorTexture != null) {
            "gl_FragColor = ${if (multiplyColor) "color * " else ""}texture2D(tex, uv);"
        } else if (color != null && !multiplyColor) {
            "gl_FragColor = color;"
        } else {
            "gl_FragColor = ${if (multiplyColor) "color * " else ""}vec4(${when (renderAttributeName) {
                uvName -> "uv, 0.0"
                positionName -> "pos"
                else -> "pos"
            } }, 1.0);"
        }
    }
}
"""

        load(vertCode, fragCode)
        if (colorTexture != null) set("tex", 0)
    }

    fun render(mesh: IMesh) {
        if (drawing) return
        drawing = true
        bind()
        set("viewProj", ActiveCamera.viewProjectionMatrix)

        val color = color
        if (color != null) set("color", color)

        val worldMatrix = worldMatrix
        if (worldMatrix != null) set("worldMatrix", worldMatrix)

        colorTexture?.bind(0)

        mesh.render(this)
        drawing = false
    }
}