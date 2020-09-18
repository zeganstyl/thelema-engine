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

package org.ksdfv.thelema.test.g3d

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.g3d.gltf.GLTF
import org.ksdfv.thelema.g3d.gltf.GLTFAssembler
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.test.Test

/** @author zeganstyl */
class GLTFAssemblerTest: Test {
    override val name: String
        get() = "glTF write test 2"

    override fun testMain() {
        val genGltf = GLTF(FS.absolute("/home/q/gltf-test-2/box.gltf"))
        genGltf.name = "box"

        val assembler = GLTFAssembler(genGltf)

        val mesh = BoxMeshBuilder().apply {
            positionName = "POSITION"
            normalName = "NORMAL"
            uvName = "TEXCOORD_0"

            material.baseColor.set(0f, 1f, 0f, 1f)
        }.build()

        val obj = Object3D()
        obj.meshes.add(mesh)

        val scene = Scene()
        scene.objects.add(obj)

        assembler.setScenes(listOf(scene))
        assembler.createBuffer()

        genGltf.directory.child("box.gltf").writeText(JSON.printObject(genGltf))
        genGltf.saveBuffers()

        val gltf = GLTF(FS.absolute("/home/q/gltf-test-2/box.gltf"))
        gltf.conf.separateThread = false
        gltf.load {
            println(gltf.materials[0].material.shader?.sourceCode())

            val camera = Camera()

            val control = OrbitCameraControl(camera = camera)
            control.listenToMouse()

            GL.glClearColor(0f, 0f, 0f, 1f)

            GL.isDepthTestEnabled = true

            GL.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                control.update(APP.deltaTime)
                camera.update()

                gltf.scene?.render()
            }
        }
    }
}