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

import app.thelema.g3d.SimpleSkybox
import app.thelema.img.CubeFrameBuffer
import app.thelema.img.ITexture

class EquirectangularToCubemapConverter: Shader(
    vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;

uniform mat4 viewProj;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}
"""
) {
    val skybox = SimpleSkybox(
        fragCode = """
varying vec3 vPosition;

uniform sampler2D equirectangularMap;

const vec2 invAtan = vec2(0.1591, 0.3183);
vec2 SampleSphericalMap(vec3 v)
{
    vec2 uv = vec2(atan(v.z, v.x), -asin(v.y));
    uv *= invAtan;
    uv += 0.5;
    return uv;
}

void main() {
    vec2 uv = SampleSphericalMap(normalize(vPosition));
    gl_FragColor = texture2D(equirectangularMap, uv);
}
"""
    )

    init {
        skybox.shader["equirectangularMap"] = 0
    }

    fun render(map: ITexture, out: CubeFrameBuffer) {
        skybox.shader.bind()
        map.bind(0)
        out.renderCube { skybox.render() }
    }

    fun render(map: ITexture, resolution: Int = 1024): CubeFrameBuffer {
        val frameBuffer = CubeFrameBuffer(resolution)
        render(map, frameBuffer)
        return frameBuffer
    }
}