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

package app.thelema.test


import app.thelema.app.APP
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.shader.Shader

/** @author zeganstyl */
class CubeModel(val shader: Shader? = defaultShader()) {
    val mesh = BoxMeshBuilder(xSize = 2f, ySize = 2f, zSize = 2f).build()

    val obj = Object3D().apply { addMesh(mesh) }

    val cubeMatrix4 = Mat4()

    val scene = Scene()

    val temp = Mat4()

    init {
        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            near = 1f
            far = 10f
            updateCamera()
        }

        scene.objects.add(obj)
    }

    fun update(delta: Float = APP.deltaTime) {
        cubeMatrix4.rotate(0f, 1f, 0f, delta)

        if (shader != null) {
            temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
        } else {
            obj.node.worldMatrix.set(cubeMatrix4)
        }
    }

    fun render() {
        if (shader != null) {
            shader.bind()
            shader["projViewModelTrans"] = temp
            mesh.render(shader)
        }
    }

    companion object {

        fun defaultShader() = Shader(
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
    }
}