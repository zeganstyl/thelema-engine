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

import org.ksdfv.thelema.ActiveCamera
import org.ksdfv.thelema.Camera
import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
class CubeModel(val shader: Shader? = defaultShader()) {
    val mesh = BoxMeshBuilder().build()

    val obj = Object3D().apply { meshes.add(mesh) }

    val cubeMatrix4 = Mat4()

    val scene = Scene()

    init {
        scene.objects.add(obj)

        ActiveCamera.api = Camera().apply {
            position.set(0f, 3f, -3f)
            direction.set(position).nor().scl(-1f)
            updateTransform()
            near = 1f
            far = 10f
            update()
        }
    }

    fun update() {
        cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

        if (shader != null) {
            obj.node.worldMatrix.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
        } else {
            obj.node.worldMatrix.set(cubeMatrix4)
        }
    }

    fun render() {
        if (shader != null) {
            shader.bind()
            shader["projViewModelTrans"] = obj.node.worldMatrix
            mesh.render(shader)
        }
    }

    companion object {
        @Language("GLSL")
        fun defaultShader() = Shader(
            vertCode = """            
attribute vec4 a_position;
varying vec3 vPosition;
uniform mat4 projViewModelTrans;

void main() {
    vPosition = a_position.xyz;
    gl_Position = projViewModelTrans * a_position;
}""",
            fragCode = """
varying vec3 vPosition;
void main() {
    gl_FragColor = vec4(vPosition, 1.0);
}""")
    }
}