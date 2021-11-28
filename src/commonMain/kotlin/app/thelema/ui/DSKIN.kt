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

import app.thelema.font.BitmapFont
import app.thelema.g2d.NinePatch
import app.thelema.g2d.Sprite
import app.thelema.g2d.TextureRegion
import app.thelema.gl.GL_NEAREST
import app.thelema.img.Image
import app.thelema.img.Texture2D
import app.thelema.res.AID
import app.thelema.res.load
import app.thelema.utils.Color
import kotlin.native.concurrent.ThreadLocal

/** Default graphic user interface skin */
@ThreadLocal
object DSKIN {
    val whiteTexture by lazy {
        Texture2D {
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
            image = Image(1, 1) {
                putRGBAs(0xFFFFFFFF.toInt())
            }
        }
    }
    val solidFrameTexture by lazy {
        Texture2D {
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
            image = Image(4, 4) {
                val b = 0xFFFFFFFF.toInt()
                val f = 0x000000EE
                putRGBAs(
                    b, b, b, b,
                    b, f, f, b,
                    b, f, f, b,
                    b, b, b, b
                )
            }
        }
    }
    val transparentFrameTexture by lazy {
        Texture2D {
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
            image = Image(4, 4) {
                val b = 0xFFFFFFFF.toInt()
                val f = 0x00000000
                putRGBAs(
                    b, b, b, b,
                    b, f, f, b,
                    b, f, f, b,
                    b, b, b, b
                )
            }
        }
    }

    val solidFrame by lazy { NinePatch(solidFrameTexture, 1, 1, 1, 1) }
    val transparentFrame by lazy { NinePatch(transparentFrameTexture, 1, 1, 1, 1) }

    val black5x5 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.BLACK
            width = 5f
            height = 5f
        }
    }

    val white5x5 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.WHITE
            width = 5f
            height = 5f
        }
    }

    val white1x1 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.WHITE
            minWidth = 1f
            minHeight = 1f
        }
    }

    val green1x1 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.GREEN
            minWidth = 1f
            minHeight = 1f
        }
    }

    val white5x5SemiTransparent by lazy {
        Sprite(whiteTexture).apply {
            color = 0xFFFFFF88.toInt()
            minWidth = 5f
            minHeight = 5f
        }
    }

    val darkGrey5x5 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.DARK_GRAY
            minWidth = 5f
            minHeight = 5f
        }
    }

    val grey5x5 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.GRAY
            minWidth = 5f
            minHeight = 5f
        }
    }

    val grey1x1 by lazy {
        Sprite(whiteTexture).apply {
            color = Color.GRAY
            minWidth = 1f
            minHeight = 1f
        }
    }

    private val plus = Sprite()

    val tree: TreeStyle by lazy {
        TreeStyle().also { treeStyle ->
            font().apply {
                onLoaded {
                    getGlyph('+')?.apply { treeStyle.plus = Sprite(TextureRegion(regions[page].texture, u, v, u2, v2)) }
                    getGlyph('-')?.apply { treeStyle.minus = Sprite(TextureRegion(regions[page].texture, u, v, u2, v2)) }
                }
            }
        }
    }

    val label: LabelStyle by lazy { LabelStyle() }

    val textButton: TextButtonStyle by lazy {
        TextButtonStyle()
    }

    val checkBox: CheckBoxStyle by lazy { CheckBoxStyle() }

    val window: WindowStyle by lazy { WindowStyle() }

    val textField: TextFieldStyle by lazy { TextFieldStyle() }

    var defaultFont: BitmapFont? = null

    fun font(): BitmapFont = defaultFont ?: AID.load("arial-15.fnt")
}
