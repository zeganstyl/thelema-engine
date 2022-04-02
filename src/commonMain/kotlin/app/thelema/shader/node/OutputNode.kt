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

package app.thelema.shader.node

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.Blending
import app.thelema.g3d.IUniformArgs
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.shader.Shader
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
class OutputNode: ShaderNode(), IRootShaderNode {
    override val componentName: String
        get() = "OutputNode"

    /** World space vertex position */
    var vertPosition by input(GLSLNode.vertex.position)

    var fragColor by input(GLSL.oneFloat)

    var alphaMode: String = Blending.OPAQUE
    var alphaCutoff: Float = 0.5f
    var cullFaceMode: Int = GL_BACK

    /** Fragments, which depth is close to camera's far clipping plane,
     * will fade out with transparency transition to zero.
     * Possible values are ranged from 0.0 to 1.0.
     * If it is not in range from 0.0 to 1.0, no transition will be applied. */
    var fadeStart = -1f
    private var cameraFarCached = -1f

    override var entityOrNull: IEntity?
        get() = super.entityOrNull
        set(value) {
            super.entityOrNull = value
            value?.component<IRootShaderNode>()?.proxy = this
        }

    override var proxy: IShaderNode?
        get() = this
        set(_) {}

    var useLogarithmicDepth: Boolean = useLogarithmicDepthByDefault

    override fun bind(uniforms: IUniformArgs) {
        super.bind(uniforms)

        if (fadeStart in 0f..1f) {
            if (cameraFarCached != ActiveCamera.far) {
                shader["fadeStart"] = ActiveCamera.far * fadeStart
                shader["fadeMul"] = 1f / (ActiveCamera.far * (1f - fadeStart))
                cameraFarCached = ActiveCamera.far
            }
        }

        GL.isBlendingEnabled = alphaMode == Blending.BLEND || fadeStart in 0f..1f
        if (GL.isBlendingEnabled) GL.setupSimpleAlphaBlending()

        val cullFace = cullFaceMode != 0
        GL.isCullFaceEnabled = cullFace
        if (cullFace) GL.cullFaceMode = cullFaceMode
    }

    override fun declarationVert(out: StringBuilder) {
        if (fadeStart in 0f..1f) {
            out.append("out float depthForFade;")
        }

        if (useLogarithmicDepth) {
            out.append("out float flogz;\n")
            out.append("const float Fcoef = 2.0 / log2($LogarithmicDepthFar + 1.0);\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        out.append("out vec4 FragColor;\n")

        if (fadeStart in 0f..1f) {
            out.append("uniform float fadeStart;\n")
            out.append("uniform float fadeMul;\n")
            out.append("in float depthForFade;\n")
        }

        if (useLogarithmicDepth) {
            out.append("in float flogz;\n")
            out.append("const float Fcoef_half = 1.0 / log2($LogarithmicDepthFar + 1.0);\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        out.append("${Shader.CLIP_SPACE_POS} = ${SceneUniforms.ViewProjMatrix.uniformName} * ${vertPosition.asVec4()};\n")
        out.append("gl_Position = ${Shader.CLIP_SPACE_POS};\n")

        if (fadeStart in 0f..1f) {
            out.append("\ndepthForFade = gl_Position.w;")
        }

        if (useLogarithmicDepth) {
            out.append("gl_Position.z = log2(max(1e-6, 1.0 + gl_Position.w)) * Fcoef - $LogarithmicDepthNear;\n")
            out.append("flogz = 1.0 + gl_Position.w;\n")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        out.append("FragColor = ${fragColor.asVec4()};\n")

        if (alphaMode == Blending.MASK) {
            out.append("\nif (FragColor.a < 0.001 || FragColor.a < $alphaCutoff) { discard; }")
            out.append("\nFragColor.a = 1.0;")
        }

        if (fadeStart in 0f..1f) {
            out.append("\nFragColor.a *= clamp(1.0 - (depthForFade - fadeStart) * fadeMul, 0.0, 1.0);")
        }

        if (useLogarithmicDepth) {
            out.append("gl_FragDepth = log2(flogz) * Fcoef_half;\n")
        }
    }

    @ThreadLocal
    companion object {
        var LogarithmicDepthNear = 0.000_001
        var LogarithmicDepthFar = 1e+9f
        var useLogarithmicDepthByDefault = true
    }
}