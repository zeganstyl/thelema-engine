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
import app.thelema.math.*
import app.thelema.shader.node.OutputNode

/** Can be used for debugging purpose */
// TODO add skinning
class SimpleShader3D(block: SimpleShader3D.() -> Unit = {}): Shader() {

    /** Set to empty string if you don't want render any attribute */
    var renderAttributeName: String = ""

    var useUvs = true
    var useNormals = true

    var color: IVec4? = null

    var worldMatrix: IMat4? = Mat4()
    val normalMatrix: Mat3 = Mat3()

    var colorTexture: ITexture2D? = null

    var alphaCutoff = 0.5f

    var lightDirection: IVec3? = Vec3(1f, 1f, 1f)
    var lightIntensity: Float = 0.5f
    var minLightIntensity: Float = 0.2f
    var maxLightIntensity: Float = 1f

    init {
        block(this)
        initShader()
    }

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
in vec3 POSITION;
${if (useUvs) "in vec2 TEXCOORD_0;" else "" }
${if (useNormals || lightDirection != null) "in vec3 NORMAL;" else "" }

out vec3 pos;
out vec2 uv;
out vec3 normal;
uniform mat4 viewProj;
uniform mat4 worldMatrix;

${if (OutputNode.useLogarithmicDepthByDefault) "out float flogz;\nconst float Fcoef = 2.0 / log2(1.0E9 + 1.0);" else ""}

void main() {
    pos = POSITION;
    ${if (useUvs) "uv = TEXCOORD_0;" else "uv = vec2(1.0);" }
    ${if (useNormals || lightDirection != null) "normal = NORMAL;" else "normal = vec3(0.5, 0.5, 0.0);"}
    normal = mat3(worldMatrix[0].xyz, worldMatrix[1].xyz, worldMatrix[2].xyz) * normal; // get normal matrix from world matrix
    normal = normalize(normal);
    gl_Position = viewProj * worldMatrix * vec4(POSITION, 1.0);
    ${
        if (OutputNode.useLogarithmicDepthByDefault) {
            "gl_Position.z = log2(max(1e-6, 1.0 + gl_Position.w)) * Fcoef - 1.0E-6;\nflogz = 1.0 + gl_Position.w;"
        } else ""
    }
}
"""
        if (fragCode.isEmpty()) fragCode = """
in vec3 pos;
in vec2 uv;
in vec3 normal;

out vec4 FragColor;

${if (lightDirection != null) "uniform vec3 lightDirection;" else ""}
uniform float lightIntensity;
uniform float minLightIntensity;
uniform float maxLightIntensity;

${if (color != null) "uniform vec4 color;" else "" }
${if (colorTexture != null) "uniform sampler2D tex;" else ""}

${if (OutputNode.useLogarithmicDepthByDefault) "in float flogz;\nconst float Fcoef_half = 1.0 / log2(1.0E9 + 1.0);" else ""}

void main() {
    vec4 fragColor = vec4(${when (renderAttributeName) {
            "TEXCOORD_0" -> "uv, 0.0, 1.0"
            "POSITION" -> "pos, 1.0"
            "NORMAL" -> "normal, 1.0"
            else -> "1.0"
        }
        });
    ${if (colorTexture != null) "fragColor *= texture2D(tex, uv);" else ""}
    ${if (color != null) "fragColor *= color;" else ""}
    ${if (lightDirection != null) "fragColor.rgb *= clamp(dot(lightDirection, normal) * lightIntensity, minLightIntensity, maxLightIntensity);" else ""}
    FragColor = fragColor;
    ${if (alphaCutoff > 0f) "if (FragColor.a < $alphaCutoff) discard; // alphaCutoff" else ""}
    ${if (OutputNode.useLogarithmicDepthByDefault) "gl_FragDepth = log2(flogz) * Fcoef_half;" else ""}
}
"""
    }

    override fun bind() {
        super.bind()

        val worldMatrix = uniformArgs?.worldMatrix ?: MATH.IdentityMat4
        set("worldMatrix", worldMatrix)

        normalMatrix.set(worldMatrix)
        set("normalMatrix", normalMatrix)

        set("viewProj", ActiveCamera.viewProjectionMatrix)

        val color = color
        if (color != null) set("color", color)

        val lightDirection = lightDirection
        if (lightDirection != null) {
            ActiveCamera.node.getDirection(lightDirection).scl(-1f)
            set("lightDirection", lightDirection)
        }
        set("lightIntensity", lightIntensity)
        set("minLightIntensity", minLightIntensity)
        set("maxLightIntensity", maxLightIntensity)

        colorTexture?.bind(0)
    }

    fun render(mesh: IMesh) {
        useShader {
            mesh.render()
        }
    }

    companion object {
        val instance = SimpleShader3D()
    }
}