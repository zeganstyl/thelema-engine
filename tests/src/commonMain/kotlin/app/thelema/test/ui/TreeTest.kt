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

package app.thelema.test.ui

import app.thelema.fs.FS
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.input.KB
import app.thelema.input.MOUSE
import app.thelema.test.Test
import app.thelema.img.Texture2D
import app.thelema.ui.*

class TreeTest: Test {
    override val name: String
        get() = "Tree"

    override fun testMain() {
        val stage = Stage(ScreenViewport())

        KB.addListener(stage)
        MOUSE.addListener(stage)

        val drawable = TextureRegionDrawable(Texture2D().load(FS.internal("thelema-logo.png")))
        drawable.minWidth = 16f
        drawable.minHeight = 16f

        val stack = Stack()
        stack.fillParent = true
        stack.addActor(Table().apply {
            defaults().pad(5f)

            val tree = Tree().apply {
                iconSpacingRight = 5f

                add(TreeNode(Label("node")).apply {
                    add(TreeNode(Label("node2")).apply { icon = drawable })
                    add(TreeNode(Label("node3")))
                })
            }
            add(tree)
        })

        stage.addActor(stack)

        GL.render {
            GL.glClearColor(0.1f, 0.1f, 0.1f, 1f)
            GL.glClear(GL_COLOR_BUFFER_BIT)

            stage.update()
            stage.render()
        }
    }
}