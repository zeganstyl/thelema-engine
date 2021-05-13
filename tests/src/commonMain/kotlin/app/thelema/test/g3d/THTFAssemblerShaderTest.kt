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

package app.thelema.test.g3d

import app.thelema.test.Test

/** @author zeganstyl */
class THTFAssemblerShaderTest: Test {
    override val name: String
        get() = "ThelemaTF assembler"

    override fun testMain() {
//        val genThtf = TETF(FS.absolute("/home/q/thtf-test/test.gltf"))
//        genThtf.name = "test"
//
//        val scene = Scene()
//
//        val box = Object3D()
//        box.add(BoxMeshBuilder().apply { positionName = "POSITION" }.build())
//
//        scene.objects.add(box)
//
//        val shader = Shader().apply {
//            val vertexNode = addNode(VertexNode())
//            val cameraDataNode = addNode(CameraDataNode(vertexNode.position))
//            addNode(OutputNode(cameraDataNode.clipSpacePosition, vertexNode.position))
//            build()
//        }
//
//        val assembler = TETFAssembler(genThtf)
//        assembler.addShader(shader)
//
//        genThtf.directory.child("test.gltf").writeText(JSON.printObject(assembler.thtf))
//        genThtf.saveBuffers()
//
//        val thtf = TETF(FS.absolute("/home/q/thtf-test/test.gltf"))
//        thtf.loadOnSeparateThread = false
//        thtf.load()
//
//        println(thtf.shaders[0].shader?.printCode())
//
//        box.meshes[0].material.shader = thtf.shaders[0].shader
//
//        val control = OrbitCameraControl()
//        control.listenToMouse()
//        println(control.help)
//
//        GL.isDepthTestEnabled = true
//        GL.glClearColor(0f, 0f, 0f, 1f)
//        GL.render {
//            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
//            control.update(APP.deltaTime)
//            ActiveCamera.update()
//
//            scene.render()
//        }
    }
}