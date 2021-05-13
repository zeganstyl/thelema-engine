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

package app.thelema.test.g3d.gltf


import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gltf.GLTF
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.math.Mat4
import app.thelema.gl.IMesh
import app.thelema.gltf.GLTFMesh
import app.thelema.gltf.GLTFPrimitive
import app.thelema.res.RES
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class GLTFLoaderTest: Test {
    override val name: String
        get() = "glTF Loader Test"

    override fun testMain() {

        val shaderProgram = Shader(
            vertCode = """            
attribute vec4 POSITION;

varying vec3 vPosition;

uniform mat4 projViewModelTrans;

void main() {
    vPosition = POSITION.xyz;
    gl_Position = projViewModelTrans * POSITION;
}""",
            fragCode = """
varying vec3 vPosition;

void main() {
    gl_FragColor = vec4(vPosition, 1.0);
}""")

        ActiveCamera {
            position.set(0f, 3f, -3f)
            direction.set(position).nor().scl(-1f)
            updateCamera()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        var mesh: IMesh? = null

        RES.loadTyped<GLTF>("gltfTest/test.gltf") {
            onLoaded {
                mesh = ((meshes[0] as GLTFMesh).primitives[0] as GLTFPrimitive).mesh
            }
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
