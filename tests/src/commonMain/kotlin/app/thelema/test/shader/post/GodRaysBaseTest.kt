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
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.GL
import app.thelema.gl.ScreenQuad
import app.thelema.img.SimpleFrameBuffer
import app.thelema.math.Vec3
import app.thelema.math.Vec4
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.post.PostShader
import app.thelema.shader.useShader
import app.thelema.test.Test

class GodRaysBaseTest: Test {
    override val name: String
        get() = "God rays base"

    override fun testMain() {
        // https://github.com/Erkaman/glsl-godrays/blob/master/index.glsl

        val godRaysShader = PostShader(
            uvName = "uv",
            fragCode = """
vec3 godrays(
    float density,
    float weight,
    float decay,
    float exposure,
    int numSamples,
    sampler2D occlusionTexture,
    vec2 screenSpaceLightPos,
    vec2 uv
) {
    vec3 fragColor = vec3(0.0,0.0,0.0);

	vec2 deltaTextCoord = vec2( uv - screenSpaceLightPos.xy );

	vec2 textCoo = uv.xy ;
	deltaTextCoord *= (1.0 /  float(numSamples)) * density;
	float illuminationDecay = 1.0;

	for (int i=0; i < 1000 ; i++) {
        /*
        This makes sure that the loop only runs `numSamples` many times.
        We have to do it this way in WebGL, since you can't have a for loop
        that runs a variable number times in WebGL.
        This little hack gets around that.
        But the drawback of this is that we have to specify an upper bound to the
        number of iterations(but 100 is good enough for almost all cases.)
        */
	    if (numSamples < i) {
            break;
	    }

		textCoo -= deltaTextCoord;
		vec3 samp = texture2D(occlusionTexture, textCoo   ).xyz;
		samp *= illuminationDecay * weight;
		fragColor += samp;
		illuminationDecay *= decay;
	}

	fragColor *= exposure;

    return fragColor;
}

uniform sampler2D occlusionTexture;
uniform sampler2D sceneTexture;
uniform vec2 screenSpaceLightPos;
in vec2 uv;

void main() {
    vec3 rays = godrays(1.0, 0.01, 1.0, 1.0, 100, occlusionTexture, screenSpaceLightPos, uv);
    gl_FragColor = texture2D(sceneTexture, uv) + vec4(rays, 0.0);
}
""")

        val occlusionTexUnit = 0
        val sceneTexUnit = 1

        godRaysShader.bind()
        godRaysShader["occlusionTexture"] = occlusionTexUnit
        godRaysShader["sceneTexture"] = sceneTexUnit

        val meshShader = Shader(
            vertCode = """
in vec3 POSITION;
in vec2 TEXCOORD_0;
out vec2 uv;

uniform mat4 viewProj;
uniform vec3 pos;

void main() {
    uv = TEXCOORD_0;
    gl_Position = viewProj * vec4(POSITION + pos, 1.0);
}""",
            fragCode = """
in vec2 uv;
out vec4 FragColor;

uniform vec4 color;
uniform bool useColor;

void main() {
    if (useColor) {
        FragColor = color;
    } else {
        FragColor = vec4(uv, 0.0, 1.0);
    }
}""")

        meshShader.bind()

        val occlusionTextureBuffer = SimpleFrameBuffer()
        val sceneTextureBuffer = SimpleFrameBuffer()

        val sphere = SphereMesh { setSize(1f) }

        val spherePos = Vec3(5f, 1f, 0f)

        val lightPos = Vec3(0f, 1f, 0f)
        val screenSpaceLightPos = Vec3()

        val lightColor = Vec4(1f, 1f, 1f, 1f)
        val blackColor = Vec4(0f, 0f, 0f, 1f)

        val box = BoxMesh { setSize(1f) }
        val boxPos = Vec3(-5f, 1f, 0f)

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            meshShader.useShader {
                meshShader["viewProj"] = ActiveCamera.viewProjectionMatrix
                meshShader["useColor"] = true

                occlusionTextureBuffer.render {
                    GL.glClear()

                    meshShader["color"] = lightColor
                    meshShader["pos"] = lightPos
                    sphere.mesh.render()

                    meshShader["color"] = blackColor

                    meshShader["pos"] = spherePos
                    sphere.mesh.render()

                    meshShader["pos"] = boxPos
                    box.mesh.render()
                }

                sceneTextureBuffer.render {
                    GL.glClear()

                    meshShader["color"] = lightColor
                    meshShader["pos"] = lightPos
                    sphere.mesh.render()

                    meshShader["useColor"] = false

                    meshShader["pos"] = spherePos
                    sphere.mesh.render()

                    meshShader["pos"] = boxPos
                    box.mesh.render()
                }
            }

            screenSpaceLightPos.set(lightPos)
            screenSpaceLightPos.prj(ActiveCamera.viewProjectionMatrix)

            // convert from NDC to screen space
            screenSpaceLightPos.scl(0.5f).add(0.5f)

            godRaysShader.bind()
            godRaysShader.set("screenSpaceLightPos", screenSpaceLightPos.x, screenSpaceLightPos.y)
            occlusionTextureBuffer.texture.bind(occlusionTexUnit)
            sceneTextureBuffer.texture.bind(sceneTexUnit)
            ScreenQuad.render(godRaysShader, null)
        }
    }
}
