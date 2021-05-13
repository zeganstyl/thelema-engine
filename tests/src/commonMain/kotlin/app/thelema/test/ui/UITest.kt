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
import app.thelema.g2d.SpriteBatch
import app.thelema.gl.GL
import app.thelema.img.Texture2D
import app.thelema.input.KB
import app.thelema.input.MOUSE
import app.thelema.test.Test
import app.thelema.ui.*
import app.thelema.utils.Color
import app.thelema.utils.LOG

class UITest: Test {
    override val name: String
        get() = "UI test"

    override fun testMain() {
        val stage = Stage(ScreenViewport()) {
            KB.addListener(this)
            MOUSE.addListener(this)

            addListener(object : InputListener {
                override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    LOG.info("stage touch down: $x, $y")
                    return true
                }

                override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                    LOG.info("stage touch drag: $x, $y")
                }

                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                    LOG.info("stage touch up: $x, $y")
                }
            })

            addActor(Stack {
                fillParent = true

                val tex = Texture2D().load("thelema-logo.png")

                addActor(Table {
                    defaults().pad(5f)

                    add(Button {
                        addAction {
                            LOG.info("Button clicked")
                        }
                    }).width(20f).height(20f)

                    add(TextButton("TextButton") {
                        addAction {
                            LOG.info("TextButton clicked")
                        }
                    })

                    add(ScrollPane(
                        Label("ScrollPane\nScrollPane\n" +
                                "ScrollPane\n" +
                                "ScrollPaneScrollPaneScrollPaneScrollPaneScrollPaneScrollPaneScrollPane\n" +
                                "ScrollPane\n" +
                                "ScrollPane\n" +
                                "ScrollPane\n" +
                                "ScrollPane\n" +
                                "ScrollPane").apply { setWrap(true) }
                    )).width(200f).height(100f)

                    add(TextField()).width(100f)

                    val progressBar = ProgressBar(0f, 1f, 0.1f, false)
                    progressBar.value = 0.3f
                    add(progressBar).width(100f)

                    val slider = Slider(0f, 1f, 0.1f, false)
                    slider.value = 0.4f
                    add(slider).width(100f)

                    add(UIImage(TextureRegionDrawable(tex)))

                    val imageButton = ImageButton()
                    imageButton.overlayImage = TextureRegionDrawable(tex)
                    add(imageButton)

                    row()

                    val list = listOf("sdfsdf", "123123")
                    add(UIList(list))

                    add(SelectBox(list).apply {
                        setSelected(list[1])
                    })

                    add(Tree {
                        add(TreeNode(Label("node")) {
                            add(TreeNode(Label("node2")))
                            add(TreeNode(Label("node3")))
                        })
                    })
                })

                addActor(Table {
                    align = Align.topLeft
                    add(MenuBar {
                        defaults().padLeft(5f).padRight(5f)
                        menu("File") {
                            menu("open") {
                                menu("recent") {
                                    item("path1") {
                                        addAction { LOG.info("clicked item: recent/path1") }
                                    }
                                    item("path2") {
                                        addAction { LOG.info("clicked item: recent/path2") }
                                    }
                                    item("path3") {
                                        addAction { LOG.info("clicked item: recent/path3") }
                                    }
                                }
                            }
                            separator()
                            item("save") {
                                addAction { LOG.info("clicked item: save") }
                            }
                            item("exit") {
                                addAction { LOG.info("clicked item: exit") }
                            }
                        }
                        menu("Edit") {}
                    }).growX()
                })
            })
        }

        GL.glClearColor(Color.GRAY)

        APP.onUpdate = { stage.update(it) }
        APP.onRender = { stage.render() }
    }
}