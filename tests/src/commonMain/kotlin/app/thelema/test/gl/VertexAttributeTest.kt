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

package app.thelema.test.gl

import app.thelema.app.APP
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.*
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import app.thelema.utils.LOG

class VertexAttributeTest: Test {
    override val name: String
        get() = "Vertex attribute"

    override fun testMain() {
        val mesh = Mesh {
            addVertexBuffer {
                addAttribute(Vertex.POSITION)
                initVertexBuffer(8) {
                    putFloats(
                        // front
                        -1f, -1f,  1f,
                        1f, -1f,  1f,
                        1f,  1f,  1f,
                        -1f,  1f,  1f,
                        // back
                        -1f, -1f, -1f,
                        1f, -1f, -1f,
                        1f,  1f, -1f,
                        -1f,  1f, -1f
                    )
                }
            }

            setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(6 * 6) {
                    putIndices(
                        // front
                        0, 1, 2,
                        2, 3, 0,
                        // right
                        1, 5, 6,
                        6, 2, 1,
                        // back
                        7, 6, 5,
                        5, 4, 7,
                        // left
                        4, 0, 3,
                        3, 7, 4,
                        // bottom
                        4, 5, 1,
                        1, 0, 4,
                        // top
                        3, 2, 6,
                        6, 7, 3
                    )
                }
            }
        }

        mesh.getAccessor(Vertex.POSITION) {
            val x = getFloat(0)
            val y = getFloat(4)
            val z = getFloat(8)

            putFloatsNext(0f, 0f, 1f)
            putFloatsNext(1f, 0f, 1f)
            putFloatsNext(1f, 1f, 1f)
            putFloatsNext(0f, 1f, 1f)

            putFloatsNext(0f, 0f, 0f)
            putFloatsNext(1f, 0f, 0f)
            putFloatsNext(1f, 1f, 0f)
            putFloatsNext(0f, 1f, 0f)

            LOG.info("$x, $y, $z")
        }

        val shader = SimpleShader3D {
            renderAttributeName = positionsName
        }

        val control = OrbitCameraControl()

        APP.onRender = {
            control.updateNow()
            mesh.render(shader)
        }
    }
}