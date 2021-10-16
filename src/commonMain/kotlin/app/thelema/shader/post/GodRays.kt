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

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.ICamera
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.math.Vec3
import app.thelema.gl.ScreenQuad

class GodRays: PostShader(
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
varying vec2 uv;

void main() {
    vec3 rays = godrays(1.0, 0.01, 1.0, 1.0, 100, occlusionTexture, screenSpaceLightPos, uv);
    gl_FragColor = texture2D(sceneTexture, uv) + vec4(rays, 0.0);
}
"""
) {
    val screenSpaceLightPos = Vec3()

    /** This texture must contain pixels, that must emitting light shafts. */
    var occlusionMap: ITexture? = null

    init {
        bind()
        this["sceneTexture"] = 0
        this["occlusionTexture"] = 1
    }

    /** Light position is a point from which rays will be emitted.
     * @param camera camera needed to project 3d light position onto 2d screen */
    fun setLightPosition(x: Float, y: Float, z: Float, camera: ICamera = ActiveCamera) {
        screenSpaceLightPos.set(x, y, z)
        screenSpaceLightPos.prj(camera.viewProjectionMatrix)

        // convert from NDC to screen space
        screenSpaceLightPos.x *= 0.5f
        screenSpaceLightPos.y *= 0.5f
        screenSpaceLightPos.x += 0.5f
        screenSpaceLightPos.y += 0.5f

        bind()
        set("screenSpaceLightPos", screenSpaceLightPos.x, screenSpaceLightPos.y)
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        (occlusionMap ?: throw IllegalStateException("GodRays: occlusionMap must be set")).bind(1)
        ScreenQuad.render(this, out)
    }
}