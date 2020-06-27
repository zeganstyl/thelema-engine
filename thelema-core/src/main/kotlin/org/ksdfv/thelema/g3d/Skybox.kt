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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.g3d.node.Node
import org.ksdfv.thelema.mesh.build.SkyBoxMeshBuilder
import org.ksdfv.thelema.shader.Shader

/** @author zeganstyl */
class Skybox: Object3D(Node()) {
    override var armature: IArmature?
        get() = null
        set(_) {}

    val mesh = SkyBoxMeshBuilder().build().apply {
        material = Material().apply {
            cullFaceMode = 0
        }
    }

    var shader: Shader?
        get() = mesh.material.shader
        set(value) { mesh.material.shader = value }

    init {
        meshes.add(mesh)
    }
}