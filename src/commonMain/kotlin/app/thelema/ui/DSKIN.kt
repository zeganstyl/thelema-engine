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

package app.thelema.ui

import app.thelema.data.DATA
import app.thelema.font.BitmapFont
import app.thelema.g2d.NinePatch
import app.thelema.g2d.Sprite
import app.thelema.gl.GL
import app.thelema.gl.GL_RGBA
import app.thelema.gl.GL_UNSIGNED_BYTE
import app.thelema.img.Texture2D
import app.thelema.res.RES

/** Default graphic user interface skin */
object DSKIN {
    val solidTexture by lazy { Texture2D(0) }
    val solidFrameTexture by lazy { Texture2D(0) }
    val transparentFrameTexture by lazy { Texture2D(0) }

    val solidFrame by lazy { NinePatchDrawable().also { initSolidFrameTexture() } }
    val transparentFrame by lazy { NinePatchDrawable().also { initTransparentFrameTexture() } }

    val black5x5 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(0f, 0f, 0f, 1f)
            minWidth = 5f
            minHeight = 5f
        }
    }

    val white5x5 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(1f, 1f, 1f, 1f)
            minWidth = 5f
            minHeight = 5f
        }
    }

    val white1x1 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(1f, 1f, 1f, 1f)
            minWidth = 1f
            minHeight = 1f
        }
    }

    val green1x1 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(0f, 1f, 0f, 1f)
            minWidth = 1f
            minHeight = 1f
        }
    }

    val white5x5SemiTransparent by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(1f, 1f, 1f, 0.5f)
            minWidth = 5f
            minHeight = 5f
        }
    }

    val grey5x5 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(0.5f, 0.5f, 0.5f, 1f)
            minWidth = 5f
            minHeight = 5f
        }
    }

    val grey1x1 by lazy {
        SpriteDrawable(Sprite(solidTexture)).apply {
            initSolidTexture()
            sprite.color.set(0.5f, 0.5f, 0.5f, 1f)
            minWidth = 1f
            minHeight = 1f
        }
    }

    private var frameInitiated = false
    private var solidInitiated = false

    private val plus = TextureRegionDrawable()

    val tree: TreeStyle by lazy { TreeStyle() }

    val label: LabelStyle by lazy { LabelStyle() }

    val textButton: TextButtonStyle by lazy {
        TextButtonStyle()
    }

    val checkBox: CheckBoxStyle by lazy { CheckBoxStyle() }

    val window: WindowStyle by lazy { WindowStyle() }

    val textField: TextFieldStyle by lazy { TextFieldStyle() }

    var defaultFont: BitmapFont? = null

    fun font(): BitmapFont = defaultFont ?: RES.loadTyped("arial-15.fnt")

    private fun initSolidTexture() {
        if (!solidInitiated) {
            solidInitiated = true
            GL.call {
                solidTexture.initOnePixelTexture(1f, 1f, 1f, 1f)
            }
        }
    }

    private fun initSolidFrameTexture() {
        if (!frameInitiated) {
            frameInitiated = true
            GL.call {
                val border = 0xFFFFFFFF.toInt()
                val fill = 0xEE000000.toInt()

                val bytes = DATA.bytes(64).apply {
                    putInts(
                        border, border, border, border,
                        border, fill, fill, border,
                        border, fill, fill, border,
                        border, border, border, border
                    )
                    rewind()
                }

                solidFrameTexture.load(4, 4, bytes, 0, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE)
                solidFrame.setPatch(NinePatch(solidFrameTexture, 1, 1, 1, 1))

                bytes.destroy()
            }
        }
    }

    private fun initTransparentFrameTexture() {
        if (!frameInitiated) {
            frameInitiated = true
            GL.call {
                val border = 0xFFFFFFFF.toInt()
                val fill = 0x00000000

                val bytes = DATA.bytes(64).apply {
                    putInts(
                        border, border, border, border,
                        border, fill, fill, border,
                        border, fill, fill, border,
                        border, border, border, border
                    )
                    rewind()
                }

                transparentFrameTexture.load(4, 4, bytes, 0, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE)
                transparentFrame.setPatch(NinePatch(transparentFrameTexture, 1, 1, 1, 1))

                bytes.destroy()
            }
        }
    }
}
