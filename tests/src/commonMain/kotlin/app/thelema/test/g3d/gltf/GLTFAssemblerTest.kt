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

import app.thelema.test.Test

/** @author zeganstyl */
class GLTFAssemblerTest: Test {
    override val name: String
        get() = "glTF write test 2"

    override fun testMain() {
//        val genGltf = GLTF(FS.absolute("/home/q/gltf-test-2/box.gltf"))
//        genGltf.name = "box"
//
//        val assembler = GLTFAssembler(genGltf)
//
//        val mesh = BoxMeshBuilder().apply {
//            positionName = "POSITION"
//            normalName = "NORMAL"
//            uvName = "TEXCOORD_0"
//
//            material.baseColor.set(0f, 1f, 0f, 1f)
//        }.build()
//
//        val obj = Object3D()
//        obj.add(mesh)
//
//        val scene = Scene()
//        scene.objects.add(obj)
//
//        assembler.setScenes(listOf(scene))
//        assembler.createBuffer()
//
//        genGltf.directory.child("box.gltf").writeText(JSON.printObject(genGltf))
//        genGltf.saveBuffers()
//
//        val gltf = GLTF(FS.absolute("/home/q/gltf-test-2/box.gltf"))
//        gltf.loadOnSeparateThread = false
//        gltf.onLoaded {
//            println((gltf.materials[0] as GLTFMaterial).material.shader?.printCode())
//
//            val control = OrbitCameraControl()
//            control.listenToMouse()
//
//            GL.glClearColor(0f, 0f, 0f, 1f)
//
//            GL.isDepthTestEnabled = true
//
//            GL.render {
//                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
//                control.update(APP.deltaTime)
//                ActiveCamera.update()
//
//                gltf.scene?.render()
//            }
//        }
//        gltf.load()
    }
}