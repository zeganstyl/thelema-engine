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

package org.ksdfv.thelema.test.mesh

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.build.FrustumMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.Color

class FrustumMeshBuilderTest: Test("Frustum Mesh Builder") {
    override fun testMain() {
        @Language("GLSL")
        val shader = Shader(
            vertCode = """
attribute vec3 aPosition;
uniform mat4 viewProj;

void main () {
    gl_Position = viewProj * vec4(aPosition, 1.0);
}""",
        fragCode = """
uniform vec4 color;
void main () {
    gl_FragColor = color;
}""")

        val perspectiveCamera = Camera(near = 0.1f, far = 1f, isOrthographic = false)

        val orthographicCamera = Camera(
            isOrthographic = true,
            viewportWidth = 1f,
            viewportHeight = 1f,
            near = 0.1f,
            far = 1f
        )

        val perspectiveFrustumMesh = FrustumMeshBuilder(perspectiveCamera.inverseViewProjectionMatrix).build()
        val orthographicFrustumMesh = FrustumMeshBuilder(orthographicCamera.inverseViewProjectionMatrix).build()

        ActiveCamera.api = Camera(
            from = Vec3(3f, 3f, -3f),
            to = Vec3(0f, 0f, 0f),
            near = 0.1f,
            far = 100f
        )

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            shader.bind()
            shader["viewProj"] = ActiveCamera.viewProjectionMatrix

            shader["color"] = Color.ORANGE
            perspectiveFrustumMesh.render(shader)

            shader["color"] = Color.GREEN
            orthographicFrustumMesh.render(shader)
        }
    }
}
