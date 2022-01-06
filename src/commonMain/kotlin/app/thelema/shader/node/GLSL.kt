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

import kotlin.native.concurrent.ThreadLocal

/** Some common objects for shader nodes
 * @author zeganstyl */
@ThreadLocal
object GLSL {
    val undefined = GLSLFloatLiteral(0f)
    val zeroFloat = GLSLFloatLiteral(0f)
    val oneFloat = GLSLFloatLiteral(1f)

    val defaultNormal = GLSLVec3Literal(0.5f, 0.5f, 1f)

    private var idCounter = 0

    fun id(): Int {
        val id = idCounter
        idCounter += 1
        return idCounter
    }

    fun resetIdCounter() {
        idCounter = 0
    }
}

@ThreadLocal
object GLSLNode {
    val vertex = VertexNode()

    val camera = CameraDataNode(vertex.position)

    val uv = UVNode()

    val particleData = ParticleDataNode()
}


val q = """
=== VERTEX SHADER ===
in vec3 POSITION;
out vec3 vPosition;
out vec3 textureCoordinates39;
out vec3 worldSpacePosition38;
out vec4 clipSpacePosition37;
out vec4 prevClipSpacePos;
uniform vec4 uSkyboxVertexTransform;
uniform mat4 uViewProjectionMatrix;
uniform mat4 uPreviousViewProjectionMatrix;
varying float depthForFade;
void main() {
vPosition = POSITION.xyz;
worldSpacePosition38 = uSkyboxVertexTransform.xyz + POSITION.xyz * uSkyboxVertexTransform.w;
clipSpacePosition37 = uViewProjectionMatrix * vec4(worldSpacePosition38, 1.0);
gl_Position = clipSpacePosition37;
depthForFade = gl_Position.w;
}

=== FRAGMENT SHADER ===
out vec3 vPosition;
vec3 textureCoordinates39;
in vec3 worldSpacePosition38;
in vec4 clipSpacePosition37;
uniform float fadeStart;
uniform float fadeMul;
varying float depthForFade;
void main() {
textureCoordinates39 = normalize(vPosition);
gl_FragColor = vec4(textureCoordinates39, 1.0);
gl_FragColor.a *= clamp(1.0 - (depthForFade - fadeStart) * fadeMul, 0.0, 1.0);
}

"""