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

import app.thelema.g3d.IScene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.IMesh
import app.thelema.img.ITexture2D
import app.thelema.math.*

/** Can be used for debugging purpose */
// TODO add skinning
class SimpleShader3D(compile: Boolean): Shader(compile = compile) {
    constructor(compile: Boolean = true, block: SimpleShader3D.() -> Unit = {}): this(compile) {
        block(this)
        initShader()
    }

    var positionsName: String = "POSITION"
    var uvsName: String = "TEXCOORD_0"
    var normalsName: String = "NORMAL"

    /** Set to empty string if you don't want render any attribute */
    var renderAttributeName: String = uvsName

    var color: IVec4? = null

    var worldMatrix: IMat4? = Mat4()
    val normalMatrix: Mat3 = Mat3()

    var colorTexture: ITexture2D? = null

    var alphaCutoff = 0.5f

    var lightDirection: IVec3? = Vec3(1f, 1f, 1f)
    var lightIntensity: Float = 1f
    var minLightIntensity: Float = 0.2f

    fun setColor(rgba8888: Int) { color = Vec4(rgba8888) }

    fun setupOnlyColor(color: IVec4, disableLight: Boolean = true) {
        if (disableLight) lightDirection = null
        alphaCutoff = 0f
        this.color = Vec4(color)
        renderAttributeName = ""
    }

    fun setupOnlyColor(rgba8888: Int, disableLight: Boolean = true) {
        if (disableLight) lightDirection = null
        alphaCutoff = 0f
        this.color = Vec4(rgba8888)
        renderAttributeName = ""
    }

    fun setupOnlyTexture(texture: ITexture2D, disableLight: Boolean = true) {
        if (disableLight) lightDirection = null
        colorTexture = texture
        color = null
        renderAttributeName = ""
    }

    fun initShader() {
        if (vertCode.isEmpty()) vertCode = """
attribute vec3 $positionsName;
${if (uvsName.isNotEmpty()) "attribute vec2 $uvsName;" else "" }
${if (normalsName.isNotEmpty() || lightDirection != null) "attribute vec3 $normalsName;" else "" }

varying vec3 pos;
varying vec2 uv;
varying vec3 normal;
uniform mat4 viewProj;
uniform mat4 worldMatrix;

void main() {
    pos = $positionsName;
    ${if (uvsName.isNotEmpty()) "uv = $uvsName;" else "uv = vec2(1.0);" }
    ${if (normalsName.isNotEmpty() || lightDirection != null) "normal = $normalsName;" else "normal = vec3(0.5, 0.5, 0.0);"}
    normal = mat3(worldMatrix[0].xyz, worldMatrix[1].xyz, worldMatrix[2].xyz) * normal; // get normal matrix from world matrix
    normal = normalize(normal);
    gl_Position = viewProj * worldMatrix * vec4($positionsName, 1.0);
}
"""
        if (fragCode.isEmpty()) fragCode = """
varying vec3 pos;
varying vec2 uv;
varying vec3 normal;

${if (lightDirection != null) "uniform vec3 lightDirection;" else ""}
uniform float lightIntensity;
uniform float minLightIntensity;

${if (color != null) "uniform vec4 color;" else "" }
${if (colorTexture != null) "uniform sampler2D tex;" else ""}

void main() {
    vec4 fragColor = vec4(${when (renderAttributeName) {
            uvsName -> "uv, 0.0, 1.0"
            positionsName -> "pos, 1.0"
            normalsName -> "normal, 1.0"
            else -> "1.0"
        }
        });
    ${if (colorTexture != null) "fragColor *= texture2D(tex, uv);" else ""}
    ${if (color != null) "fragColor *= color;" else ""}
    ${if (lightDirection != null) "fragColor.rgb *= max(dot(lightDirection, normal) * lightIntensity, minLightIntensity);" else ""}
    gl_FragColor = fragColor;
    ${if (alphaCutoff > 0f) "if (gl_FragColor.a < $alphaCutoff) discard; // alphaCutoff" else ""}
}
"""

        load(vertCode, fragCode)
        if (colorTexture != null) set("tex", 0)
    }

    override fun prepareShader(mesh: IMesh, scene: IScene?) {
        super.prepareShader(mesh, scene)
        set("viewProj", ActiveCamera.viewProjectionMatrix)

        val color = color
        if (color != null) set("color", color)

        val worldMatrix = mesh.worldMatrix ?: MATH.IdentityMat4
        set("worldMatrix", worldMatrix)

        normalMatrix.set(worldMatrix)
        set("normalMatrix", normalMatrix)

        val lightDirection = lightDirection
        if (lightDirection != null) {
            lightDirection.set(ActiveCamera.node.position).nor()
            set("lightDirection", lightDirection)
        }
        set("lightIntensity", lightIntensity)
        set("minLightIntensity", minLightIntensity)

        colorTexture?.bind(0)
    }

    fun render(mesh: IMesh) {
        mesh.render(this)
    }

    companion object {
        val instance: SimpleShader3D by lazy { SimpleShader3D() }
    }
}