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

package org.ksdfv.thelema.shader.node

import org.ksdfv.thelema.kx.Language
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.g3d.light.DirectionalLight
import org.ksdfv.thelema.g3d.light.LightType
import org.ksdfv.thelema.g3d.light.PointLight
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.mesh.IMesh

// TODO https://github.com/KhronosGroup/glTF-Sample-Viewer/blob/master/src/shaders/pbr.frag

/** @author zeganstyl */
class PrincipledBSDF: ShaderNode() {
    override val name: String
        get() = "Principled BSDF"

    override val classId: String
        get() = ClassId

    override val inputForm: Map<String, Int>
        get() = InputForm

    var worldPosition: IShaderData
        get() = input[WorldPosition] ?: GLSL.zeroFloat
        set(value) = setInput(WorldPosition, value)

    var normalizedViewVector: IShaderData
        get() = input[NormalizedViewVector] ?: GLSL.zeroFloat
        set(value) = setInput(NormalizedViewVector, value)

    var baseColor: IShaderData
        get() = input[BaseColor] ?: GLSL.oneFloat
        set(value) = setInput(BaseColor, value)

    var normal: IShaderData
        get() = input[Normal] ?: GLSL.defaultNormal
        set(value) = setInput(Normal, value)

    var occlusion: IShaderData
        get() = input[Occlusion] ?: GLSL.oneFloat
        set(value) = setInput(Occlusion, value)

    var roughness: IShaderData
        get() = input[Roughness] ?: GLSL.oneFloat
        set(value) = setInput(Roughness, value)

    var metallic: IShaderData
        get() = input[Metallic] ?: GLSL.zeroFloat
        set(value) = setInput(Metallic, value)

    var clipSpacePosition: IShaderData
        get() = input[ClipSpacePosition] ?: GLSL.zeroFloat
        set(value) = setInput(ClipSpacePosition, value)

    val result: IShaderData = defOut(GLSLVec4("result"))

    var maxNumDirectionLights: Int = 2
    var maxNumPointLights: Int = 20
    var maxNumSpotLights: Int = 2

    var receiveShadows: Boolean = false

    private var tmpScene: IScene? = null

    override fun read(json: IJsonObject) {
        super.read(json)

        maxNumDirectionLights = json.int("maxNumDirectionLights", 2)
        maxNumPointLights = json.int("maxNumPointLights", 20)
        maxNumSpotLights = json.int("maxNumSpotLights", 2)
        receiveShadows = json.bool("receiveShadows", false)
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        json["maxNumDirectionLights"] = maxNumDirectionLights
        json["maxNumPointLights"] = maxNumPointLights
        json["maxNumSpotLights"] = maxNumSpotLights
        json["receiveShadows"] = receiveShadows
    }

    override fun prepareToBuild() {
        super.prepareToBuild()

        if (clipSpacePosition === GLSL.zeroFloat && receiveShadows) {
            throw IllegalStateException("PrincipledBSDF: receiveShadows enabled, but clipSpacePos is not set")
        }
    }

    override fun prepareToDrawScene(scene: IScene) {
        super.prepareToDrawScene(scene)

        tmpScene = scene

        if (result.isUsed) {
            var dirLightIndex = 0
            var pointLightIndex = 0
            var spotLightIndex = 0
            val lights = scene.lights
            for (i in lights.indices) {
                val light = lights[i]
                if (light.isLightEnabled) {
                    when (light.lightType) {
                        LightType.Directional -> {
                            light as DirectionalLight
                            shader["uDirLightDirection[$dirLightIndex]"] = light.direction
                            shader["uDirLightColor[$dirLightIndex]"] = light.color
                            shader["uDirLightIntensity[$dirLightIndex]"] = light.lightIntensity

                            if (light.isShadowEnabled) {
                                val lightCascadesStartIndex = dirLightIndex * light.shadowCascadesNum
                                for (j in 0 until light.shadowCascadesNum) {
                                    val cascadeIndex = lightCascadesStartIndex + j
                                    shader["uDirLightCascadeEnd[$cascadeIndex]"] = light.shadowCascadeEnd[j]
                                    shader["uDirLightViewProj[$cascadeIndex]"] = light.viewProjectionMatrices[j]
                                }
                            }

                            dirLightIndex++
                        }
                        LightType.Point -> {
                            light as PointLight
                            shader["uPointLightPosition[$pointLightIndex]"] = light.position
                            shader["uPointLightColor[$pointLightIndex]"] = light.color
                            shader["uPointLightIntensity[$pointLightIndex]"] = light.lightIntensity
                            shader["uPointLightRange[$pointLightIndex]"] = light.range
                            pointLightIndex++
                        }
                    }
                }
            }

            shader["uDirLightsNum"] = dirLightIndex
            shader["uPointLightsNum"] = pointLightIndex
            shader["uSpotLightsNum"] = spotLightIndex

            shader["uAmbientColor"] = scene.world.ambientColor
        }
    }

    override fun prepareToDrawMesh(mesh: IMesh) {
        super.prepareToDrawMesh(mesh)

        // texture binding must be on each mesh

        if (result.isUsed) {
            val scene = tmpScene!!

            var dirLightIndex = 0
            var pointLightIndex = 0
            var spotLightIndex = 0
            val lights = scene.lights
            for (i in lights.indices) {
                val light = lights[i]
                if (light.isLightEnabled) {
                    when (light.lightType) {
                        LightType.Directional -> {
                            light as DirectionalLight
                            if (light.isShadowEnabled) {
                                val lightCascadesStartIndex = dirLightIndex * light.shadowCascadesNum
                                val shadowMaps = light.shadowMaps
                                for (j in 0 until light.shadowCascadesNum) {
                                    val cascadeIndex = lightCascadesStartIndex + j
                                    val unit = GL.getNextTextureUnit()
                                    shader["uDirLightShadowMap[$cascadeIndex]"] = unit
                                    shadowMaps[j].bind(unit)
                                }
                            }

                            dirLightIndex++
                        }
                        LightType.Point -> {
                            light as PointLight
                            pointLightIndex++
                        }
                    }
                }
            }
        }
    }

    override fun executionFrag(out: StringBuilder) {
        if (result.isUsed) {
            out.append("${result.ref} = principledBSDFMain(${worldPosition.asVec3()}, ${normalizedViewVector.asVec3()}, ${baseColor.asVec4()}, ${normal.asVec3()}, ${occlusion.asFloat()}, ${roughness.asFloat()}, ${metallic.asFloat()});\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (result.isUsed) {
            if (receiveShadows) {
                val num = maxNumDirectionLights * shadowCascadesNum
                out.append("uniform sampler2D uDirLightShadowMap[$num];\n")
                out.append("uniform float uDirLightCascadeEnd[$num];\n")
                out.append("$varIn vec4 vDirLightClipSpacePos[$num];\n")
            }

            out.append("${result.typedRef} = ${result.typeStr}(0.0);\n")
            out.append(pbrCode(maxNumDirectionLights, maxNumPointLights, maxNumSpotLights, clipSpacePosition.ref, receiveShadows))
        }
    }

    override fun declarationVert(out: StringBuilder) {
        if (result.isUsed) {
            if (receiveShadows) {
                val num = maxNumDirectionLights * shadowCascadesNum
                out.append("uniform mat4 uDirLightViewProj[$num];\n")
                out.append("$varOut vec4 vDirLightClipSpacePos[$num];\n")
            }
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (result.isUsed) {
            if (receiveShadows) {
                val num = maxNumDirectionLights * shadowCascadesNum
                out.append("for (int i = 0; i < $num; i++) {\n")
                out.append("vDirLightClipSpacePos[i] = uDirLightViewProj[i] * ${worldPosition.asVec4()};\n")
                out.append("}\n")
            }
        }
    }

    companion object {
        val shadowCascadesNum = 4

        const val ClassId = "principledBSDF"

        const val WorldPosition = "worldPosition"
        const val NormalizedViewVector = "normalizedViewVector"
        const val BaseColor = "baseColor"
        const val Normal = "normal"
        const val Occlusion = "occlusion"
        const val Roughness = "roughness"
        const val Metallic = "metallic"
        const val ClipSpacePosition = "clipSpacePosition"

        val InputForm = LinkedHashMap<String, Int>().apply {
            put(WorldPosition, GLSLType.Vec3)
            put(NormalizedViewVector, GLSLType.Vec3)
            put(BaseColor, GLSLType.Vec4)
            put(Normal, GLSLType.Vec3)
            put(Occlusion, GLSLType.Float)
            put(Roughness, GLSLType.Float)
            put(Metallic, GLSLType.Float)
            put(ClipSpacePosition, GLSLType.Vec4)
        }

        @Language("GLSL")
        fun cascadedShadowsExe(
            maxNumDirLights: Int,
            clipSpacePos: String,
            use: Boolean = false,
            visualizeCascadeFields: Boolean = false
        ): String {
            return if (use) """
// cascaded shadow
${if (visualizeCascadeFields) "vec3 cascadeColor = vec3(0.0);" else ""}
int lightIndex = i * $shadowCascadesNum;
for (int j = 0; j < ${shadowCascadesNum * maxNumDirLights}; j++) {
    // this index comparison is for webgl
    if ($clipSpacePos.z < uDirLightCascadeEnd[j] && j >= lightIndex && j < lightIndex + $shadowCascadesNum) {
        ${if (visualizeCascadeFields) """
        if (j == 0) {
            cascadeColor = vec3(1.0, 0.0, 0.0);
        } else if (j == 1) {
            cascadeColor = vec3(0.0, 1.0, 0.0);
        } else if (j == 2) {
            cascadeColor = vec3(0.0, 0.0, 1.0);       
        } else if (j == 3) {
            cascadeColor = vec3(1.0, 1.0, 0.0);       
        }
        """ else ""}
        
        vec4 lightSpacePos = vDirLightClipSpacePos[j];
        vec3 projCoords = (lightSpacePos.xyz / lightSpacePos.w) * 0.5 + 0.5;
        
        float closestDepth = texture2D(uDirLightShadowMap[j], projCoords.xy).x;        
        light *= 1.0 - shadowFactor(projCoords.z, closestDepth);
        ${if (visualizeCascadeFields) "light += cascadeColor * 0.5;" else ""}
        
        break;
    }
}
""" else ""
        }

        @Language("GLSL")
        fun pbrCode(
            maxNumDirLights: Int,
            maxNumPointLights: Int,
            maxNumSpotLights: Int,
            clipSpacePos: String,
            receiveShadow: Boolean
        ): String {
            return """
//float CalcShadowFactor(int index, vec4 lightSpacePos) {
//    vec3 projCoords = (lightSpacePos.xyz / lightSpacePos.w) * 0.5 + 0.5;
//
//    float currentDepth = projCoords.z;
//    float closestDepth = texture2D(shadowMap[index], projCoords).x;
//
//    float bias = 0.001;
//    if (currentDepth - bias > closestDepth)
//        return 0.5;
//    else
//        return 0.0;
//}

float shadowFactor(float currentDepth, float closestDepth) {
    float bias = 0.001;
    if (currentDepth - bias > closestDepth)
        return 1.0;
    else
        return 0.0;
}

uniform int uDirLightsNum;
uniform vec3 uDirLightDirection[$maxNumDirLights];
uniform vec3 uDirLightColor[$maxNumDirLights];
uniform float uDirLightIntensity[$maxNumDirLights];

uniform int uPointLightsNum;
uniform vec3 uPointLightColor[$maxNumPointLights];
uniform float uPointLightRange[$maxNumPointLights];
uniform float uPointLightIntensity[$maxNumPointLights];
uniform vec3 uPointLightPosition[$maxNumPointLights];

uniform int numSpotLights;
uniform vec3 uSpotLightColor[$maxNumSpotLights];
uniform float uSpotLightRange[$maxNumSpotLights];
uniform float uSpotLightIntensity[$maxNumSpotLights];
uniform vec3 uSpotLightPosition[$maxNumSpotLights];
uniform vec3 uSpotLightDirection[$maxNumDirLights];
uniform float uSpotLightInnerConeCos[$maxNumSpotLights];
uniform float uSpotLightInnerConeCos2[$maxNumSpotLights];
uniform float uSpotLightOuterConeCos[$maxNumSpotLights];

uniform vec3 uAmbientColor;

const float M_PI = 3.141592653589793;
const float c_MinReflectance = 0.04;

// functions =======================================
struct AngularInfo
{
    float NdotL;                  // cos angle between normal and light direction
    float NdotV;                  // cos angle between normal and view direction
    float NdotH;                  // cos angle between normal and half vector
    float LdotH;                  // cos angle between light direction and half vector

    float VdotH;                  // cos angle between view direction and half vector

    vec3 padding;
};

float getPerceivedBrightness(vec3 vector)
{
    return sqrt(0.299 * vector.r * vector.r + 0.587 * vector.g * vector.g + 0.114 * vector.b * vector.b);
}

// https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_pbrSpecularGlossiness/examples/convert-between-workflows/js/three.pbrUtilities.js#L34
float solveMetallic(vec3 diffuse, vec3 specular, float oneMinusSpecularStrength) {
    float specularBrightness = getPerceivedBrightness(specular);

    if (specularBrightness < c_MinReflectance) {
        return 0.0;
    }

    float diffuseBrightness = getPerceivedBrightness(diffuse);

    float a = c_MinReflectance;
    float b = diffuseBrightness * oneMinusSpecularStrength / (1.0 - c_MinReflectance) + specularBrightness - 2.0 * c_MinReflectance;
    float c = c_MinReflectance - specularBrightness;
    float D = b * b - 4.0 * a * c;

    return clamp((-b + sqrt(D)) / (2.0 * a), 0.0, 1.0);
}

AngularInfo getAngularInfo(vec3 pointToLight, vec3 normal, vec3 view)
{
    // Standard one-letter names
    vec3 n = normalize(normal);           // Outward direction of surface point
    vec3 v = view;             // Direction from surface point to view
    vec3 l = normalize(pointToLight);     // Direction from surface point to light
    vec3 h = normalize(l + v);            // Direction of the vector between l and v

    float NdotL = clamp(dot(n, l), 0.1, 1.0);
    float NdotV = clamp(dot(n, v), 0.1, 1.0);
    float NdotH = clamp(dot(n, h), 0.1, 1.0);
    float LdotH = clamp(dot(l, h), 0.1, 1.0);
    float VdotH = clamp(dot(v, h), 0.1, 1.0);

    return AngularInfo(
    NdotL,
    NdotV,
    NdotH,
    LdotH,
    VdotH,
    vec3(0, 0, 0)
    );
}
// /functions ======================================

struct MaterialInfo
{
    float perceptualRoughness;    // roughness value, as authored by the model creator (input to shader)
    vec3 reflectance0;            // full reflectance color (normal incidence angle)

    float alphaRoughness;         // roughness mapped to a more linear change in the roughness (proposed by [2])
    vec3 diffuseColor;            // color contribution from diffuse lighting

    vec3 reflectance90;           // reflectance color at grazing angle
    vec3 specularColor;           // color contribution from specular lighting
};

// Lambert lighting
// see https://seblagarde.wordpress.com/2012/01/08/pi-or-not-to-pi-in-game-lighting-equation/
vec3 diffuse(MaterialInfo materialInfo)
{
    return materialInfo.diffuseColor / M_PI;
}

// The following equation models the Fresnel reflectance term of the spec equation (aka F())
// Implementation of fresnel from [4], Equation 15
vec3 specularReflection(MaterialInfo materialInfo, AngularInfo angularInfo)
{
    return materialInfo.reflectance0 + (materialInfo.reflectance90 - materialInfo.reflectance0) * pow(clamp(1.0 - angularInfo.VdotH, 0.0, 1.0), 5.0);
}

// Smith Joint GGX
// Note: Vis = G / (4 * NdotL * NdotV)
// see Eric Heitz. 2014. Understanding the Masking-Shadowing Function in Microfacet-Based BRDFs. Journal of Computer Graphics Techniques, 3
// see Real-Time Rendering. Page 331 to 336.
// see https://google.github.io/filament/Filament.md.html#materialsystem/specularbrdf/geometricshadowing(specularg)
float visibilityOcclusion(MaterialInfo materialInfo, AngularInfo angularInfo)
{
    float NdotL = angularInfo.NdotL;
    float NdotV = angularInfo.NdotV;
    float alphaRoughnessSq = materialInfo.alphaRoughness * materialInfo.alphaRoughness;

    float GGXV = NdotL * sqrt(NdotV * NdotV * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);
    float GGXL = NdotV * sqrt(NdotL * NdotL * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);

    float GGX = GGXV + GGXL;
    if (GGX > 0.0)
    {
        return 0.5 / GGX;
    }
    return 0.0;
}

// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
float microfacetDistribution(MaterialInfo materialInfo, AngularInfo angularInfo)
{
    float alphaRoughnessSq = materialInfo.alphaRoughness * materialInfo.alphaRoughness;
    float f = (angularInfo.NdotH * alphaRoughnessSq - angularInfo.NdotH) * angularInfo.NdotH + 1.0;
    return alphaRoughnessSq / (M_PI * f * f);
}

vec3 getPointShade(vec3 pointToLight, MaterialInfo materialInfo, vec3 normal, vec3 view)
{
    AngularInfo angularInfo = getAngularInfo(pointToLight, normal, view);

    if (angularInfo.NdotL > 0.0 || angularInfo.NdotV > 0.0)
    {
        // Calculate the shading terms for the microfacet specular shading model
        vec3 F = specularReflection(materialInfo, angularInfo);
        float Vis = visibilityOcclusion(materialInfo, angularInfo);
        float D = microfacetDistribution(materialInfo, angularInfo);

        // Calculation of analytical lighting contribution
        vec3 diffuseContrib = (1.0 - F) * diffuse(materialInfo);
        vec3 specContrib = F * Vis * D;

        // Obtain final intensity as reflectance (BRDF) scaled by the energy of the light (cosine law)
        return angularInfo.NdotL * (diffuseContrib + specContrib);
    }

    return vec3(0.0, 0.0, 0.0);
}

// https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#range-property
float getRangeAttenuation(float range, float distance)
{
    if (range <= 0.0)
    {
        // negative range means unlimited
        return 1.0;
    }
    //return max(min(1.0 - pow(distance / range, 4.0), 1.0), 0.0) / pow(distance, 2.0);
    return range / distance;
}

// https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
float getSpotAttenuation(vec3 pointToLight, vec3 spotDirection, float outerConeCos, float innerConeCos)
{
    float actualCos = dot(normalize(spotDirection), normalize(-pointToLight));
    if (actualCos > outerConeCos)
    {
        if (actualCos < innerConeCos)
        {
            return smoothstep(outerConeCos, innerConeCos, actualCos);
        }
        return 1.0;
    }
    return 0.0;
}

/*
worldPosition - vertex position
viewToPosition = normalize(cameraPosition - worldPosition)
*/
vec4 principledBSDFMain(vec3 worldPosition, vec3 viewToPosition, vec4 baseColor, vec3 Normal, float occlusion, float perceptualRoughness, float metallic) {
    // Metallic and Roughness material properties are packed together
    // In glTF, these factors can be specified by fixed scalar values
    // or from a metallic-roughness map

    vec3 diffuseColor = vec3(0.0);
    vec3 specularColor = vec3(0.0);
    vec3 f0 = vec3(0.04);

    //baseColor = SRGBtoLINEAR(Diffuse);

    diffuseColor = baseColor.rgb * (vec3(1.0) - f0) * (1.0 - metallic);

    specularColor = mix(f0, baseColor.rgb, metallic);

    #ifdef MATERIAL_UNLIT
    gl_FragColor = vec4(LINEARtoSRGB(baseColor.rgb), baseColor.a);
    //return;
    #endif

    perceptualRoughness = clamp(perceptualRoughness, 0.0, 1.0);
    metallic = clamp(metallic, 0.0, 1.0);

    // Roughness is authored as perceptual roughness; as is convention,
    // convert to material roughness by squaring the perceptual roughness [2].
    float alphaRoughness = perceptualRoughness * perceptualRoughness;

    // Compute reflectance.
    float reflectance = max(max(specularColor.r, specularColor.g), specularColor.b);

    vec3 specularEnvironmentR0 = specularColor.rgb;
    // Anything less than 2% is physically impossible and is instead considered to be shadowing. Compare to "Real-Time-Rendering" 4th editon on page 325.
    vec3 specularEnvironmentR90 = vec3(clamp(reflectance * 50.0, 0.0, 1.0));

    MaterialInfo materialInfo = MaterialInfo(
    perceptualRoughness,
    specularEnvironmentR0,
    alphaRoughness,
    diffuseColor,
    specularEnvironmentR90,
    specularColor
    );

    // LIGHTING

    vec3 color = vec3(0.0, 0.0, 0.0);
    vec3 normal = Normal;
    vec3 view = viewToPosition;

    // https://stackoverflow.com/questions/38986208/webgl-loop-index-cannot-be-compared-with-non-constant-expression
    for (int i=0; i<$maxNumDirLights; i++) {
        if (i >= uDirLightsNum){break;}

        vec3 pointToLight = -uDirLightDirection[i];
        vec3 shade = getPointShade(pointToLight, materialInfo, normal, view);
        vec3 light = uDirLightIntensity[i] * uDirLightColor[i] * shade;
        
        ${cascadedShadowsExe(maxNumDirLights, clipSpacePos, receiveShadow)}

        color += light;
    }
    
    for (int i=0; i<$maxNumPointLights; i++) {
        if (i >= uPointLightsNum){break;}

        vec3 pointToLight = uPointLightPosition[i] - worldPosition;
        float distance = length(pointToLight);
        float attenuation = getRangeAttenuation(uPointLightRange[i], distance);
        vec3 shade = getPointShade(pointToLight, materialInfo, normal, view);
        
        color += attenuation * uPointLightIntensity[i] * uPointLightColor[i] * shade;
    }
    
    for (int i=0; i<$maxNumSpotLights; i++) {
        if (i >= numSpotLights){break;}
        
        vec3 pointToLight = uSpotLightPosition[i] - worldPosition;
        float distance = length(pointToLight);
        float rangeAttenuation = getRangeAttenuation(uSpotLightRange[i], distance);
        float spotAttenuation = getSpotAttenuation(pointToLight, uSpotLightDirection[i], uSpotLightOuterConeCos[i], uSpotLightInnerConeCos[i]);
        vec3 shade = getPointShade(pointToLight, materialInfo, normal, view);
        color += rangeAttenuation * spotAttenuation * uSpotLightIntensity[i] * uSpotLightColor[i] * shade;
    }
    
    if (uDirLightsNum == 0 && uPointLightsNum == 0 && numSpotLights == 0) { 
        color += baseColor.rgb;
    }

    vec3 ambient = uAmbientColor * baseColor.rgb * occlusion;
    color += ambient;

    // regular shading
    return vec4(color, baseColor.a);
}
"""
        }
    }
}
