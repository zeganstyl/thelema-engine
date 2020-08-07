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

package org.ksdfv.thelema.test

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.gltf.GLTF
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.shader.Shader

/** @author zeganstyl */
class GLTFLoaderTest: Test {
    override val name: String
        get() = "glTF Loader Test"

    override fun testMain() {
        @Language("GLSL")
        val shaderProgram = Shader(
            vertCode = """            
attribute vec4 aPosition;

varying vec3 vPosition;

uniform mat4 projViewModelTrans;

void main() {
    vPosition = aPosition.xyz;
    gl_Position = projViewModelTrans * aPosition;
}""",
            fragCode = """
varying vec3 vPosition;

void main() {
    gl_FragColor = vec4(vPosition, 1.0);
}""")

        ActiveCamera.api = Camera().apply {
            position.set(0f, 3f, -3f)
            direction.set(position).nor().scl(-1f)
            update()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        var mesh: IMesh? = null

        val gltf = GLTF(FS.internal("gltfTest/test.gltf"))
        gltf.load {
            mesh = gltf.objects[0].meshes[0]
        }

        GL.isDepthTestEnabled = true

        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

            shaderProgram.bind()
            shaderProgram["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
            mesh?.render(shaderProgram)
        }
    }
}
