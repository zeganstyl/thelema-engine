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

/** @author zeganstyl */
class NormalMapNode(vertexPosition: IShaderData = GLSLNode.vertex.position): ShaderNode() {
    constructor(block: NormalMapNode.() -> Unit): this() { block(this) }

    override val componentName: String
        get() = "NormalMapNode"

    /** normalize(cameraPosition - worldPosition), for back-facing surface */
    var normalizedViewVector by input(GLSL.zeroFloat)

    /** World space vertex position */
    var vertexPosition by input(GLSL.zeroFloat)

    /** Texture coordinates */
    var uv by input(GLSLNode.uv.uv)

    /** Optional. Normal scaling float value */
    var normalScale by input(GLSL.oneFloat)

    /** Normal from texture */
    var normalColor by input(GLSL.defaultNormal)

    /** Matrix 3x3 with tangent, binormal, normal vectors */
    var tbn by input(GLSL.zeroFloat)

    val tangentResult = output(GLSLVec3("tangent"))
    val biNormalResult = output(GLSLVec3("biNormal"))
    val normalResult = output(GLSLVec3("normal"))

    init {
        uv = GLSLNode.uv.uv
        this.vertexPosition = vertexPosition
        tbn = GLSLNode.vertex.tbn
        normalizedViewVector = GLSLNode.camera.normalizedViewVector
    }

    override fun executionFrag(out: StringBuilder) {
        if (normalResult.isUsed || tangentResult.isUsed || biNormalResult.isUsed) {
            out.append("normalMapMain(${normalizedViewVector.asVec3()}, ${tbn.ref}, ${vertexPosition.asVec3()}, ${uv.asVec2()}, ${normalScale.asFloat()}, ${normalColor.asVec3()});\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (normalResult.isUsed || tangentResult.isUsed || biNormalResult.isUsed) {
            out.append(normalCode(tangentResult.ref, biNormalResult.ref, normalResult.ref))
            out.append('\n')
        }
    }

    companion object {
        // TODO https://github.com/KhronosGroup/glTF-Sample-Viewer/blob/master/src/shaders/pbr.frag
        fun normalCode(outTangentName: String, outBiNormalName: String, outNormalName: String): String {
            return """
vec3 $outTangentName = vec3(0.0);
vec3 $outBiNormalName = vec3(0.0);
vec3 $outNormalName = vec3(0.0);

/*
viewDir = normalize(cameraPosition - worldPosition), need for back-facing surface
normal - vertex normal
worldPosition - vertex position
uv - texture coordinates
normalScale - normal texture scaling. Used if normalTexValue is not vec3(0.0, 0.0, 0.0)
normalTexValue - RGB value from normal texture. May be vec3(0.0, 0.0, 0.0) and it will be not used.
 */
void normalMapMain(vec3 viewVector, mat3 tbn, vec3 worldPosition, vec2 uv, float normalScale, vec3 colorValue) {
    vec3 t = tbn[0];
    vec3 b = tbn[1];
    vec3 ng = tbn[2];

    // For a back-facing surface, the tangential basis vectors are negated.
//    float facing = step(0.0, dot(viewVector, ng)) * 2.0 - 1.0;
//    t *= facing;
//    b *= facing;
//    ng *= facing;
    // For a back-facing surface, the tangential basis vectors are negated.
    if (gl_FrontFacing == false)
    {
        t *= -1.0;
        b *= -1.0;
        ng *= -1.0;
    }

    vec3 n = colorValue * 2.0 - vec3(1.0);
    n *= vec3(normalScale, normalScale, 1.0);
    n = mat3(t, b, ng) * normalize(n);

    $outTangentName = t;
    $outBiNormalName = b;
    $outNormalName = n;
}"""
        }
    }
}
