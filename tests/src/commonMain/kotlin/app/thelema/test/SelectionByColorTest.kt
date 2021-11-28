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
import app.thelema.data.DATA
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.*
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.input.BUTTON
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.utils.Color
import app.thelema.utils.LOG

class SelectionByColorTest: Test {
    override fun testMain() {
        val plane = PlaneMesh { setSize(10f) }
        val box = BoxMesh { setSize(2f) }
        val sphere = SphereMesh { setSize(1.3f) }

        val colorMap = HashMap<Int, IMesh>()
        val meshes = arrayOf(plane.mesh, box.mesh, sphere.mesh)
        var selected: IMesh? = null
        val highlightColor = Vec4(1f, 0.8f, 0.4f, 1f)

        val sceneShader = SimpleShader3D {
            renderAttributeName = ""
            color = Vec4()
        }

        val selectionRenderShader = SimpleShader3D {
            alphaCutoff = 0f
            lightDirection = null
            renderAttributeName = ""
            color = Vec4()
        }

        val pixels = DATA.bytes(GL.mainFrameBufferWidth * GL.mainFrameBufferHeight * 4)
        var selectRequest = false
        var pixelStart = 0

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                if (button != BUTTON.RIGHT) return
                pixelStart = ((APP.height - screenY) * GL.mainFrameBufferWidth + screenX) * 4
                selectRequest = true
            }
        })

        LOG.info("Use right mouse button to select")

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            selectionRenderShader.bind()

            if (selectRequest) {
                selectRequest = false

                // render to main frame buffer and read pixels from it
                var color = 0x000001FFL
                var j = 1
                for (i in meshes.indices) {
                    val mesh = meshes[i]
                    colorMap[j] = mesh
                    selectionRenderShader.color?.also { Color.rgba8888ToColor(it, color.toInt()) }
                    mesh.render(selectionRenderShader)
                    color += 256
                    j++
                }
                GL.glReadPixels(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
                GL.glClearColor(Color.BLACK)
                GL.glClear()

                val colorId = pixels[pixelStart].toInt() * 65025 + pixels[pixelStart + 1].toInt() * 255 + pixels[pixelStart + 2].toInt()

                selected = colorMap[colorId]
            }

            val defaultColor = Vec4(0.75f, 0.75f, 0.75f, 1f)

            // render scene
            for (i in meshes.indices) {
                val mesh = meshes[i]
                sceneShader.color?.set(if (selected == mesh) highlightColor else defaultColor)
                mesh.render(sceneShader)
            }
        }
    }
}