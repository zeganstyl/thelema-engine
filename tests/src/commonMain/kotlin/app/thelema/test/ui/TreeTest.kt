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

import app.thelema.app.APP
import app.thelema.g2d.Sprite
import app.thelema.gl.GL
import app.thelema.test.Test
import app.thelema.img.Texture2D
import app.thelema.input.KB
import app.thelema.input.MOUSE
import app.thelema.ui.*
import app.thelema.utils.Color

class TreeTest: Test {
    override val name: String
        get() = "Tree"

    override fun testMain() {
        val stage = HeadUpDisplay()

        KB.addListener(stage)
        MOUSE.addListener(stage)

        val drawable = Sprite(Texture2D("thelema-logo-64.png"))
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

        GL.glClearColor(Color.GRAY)

        APP.onRender = {
            stage.update()
            stage.render()
        }
    }
}