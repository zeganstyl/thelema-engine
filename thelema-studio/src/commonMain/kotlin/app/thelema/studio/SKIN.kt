package app.thelema.studio

import app.thelema.g2d.NinePatch
import app.thelema.g2d.Sprite
import app.thelema.gl.GL_NEAREST
import app.thelema.img.Image
import app.thelema.img.Texture2D
import app.thelema.ui.CheckBoxStyle
import app.thelema.ui.DSKIN
import app.thelema.ui.ScrollPaneStyle
import app.thelema.utils.Color

object SKIN {
    var overColor = Color.rgba8888(0f, 0.5f, 0.25f, 1f)
    var lineColor = Color.rgba8888(0.3f, 0.3f, 0.3f, 1f)

    val background = Sprite(DSKIN.whiteTexture).apply {
        color = Color.rgba8888(0f, 0f, 0f, 0.8f)
        minWidth = 0f
        minHeight = 0f
    }

    val titleBackground = Sprite(DSKIN.whiteTexture).apply {
        color = Color.mulAlpha(Color.DARK_GRAY, 0.8f)
        minWidth = 0f
        minHeight = 0f
    }

    val overBackground = Sprite(DSKIN.whiteTexture).apply {
        color = Color.mulAlpha(overColor, 0.25f)
        minWidth = 0f
        minHeight = 0f
    }

    val hLine = Sprite(DSKIN.whiteTexture).apply { color = lineColor; minHeight = 4f }

    val scroll = ScrollPaneStyle().apply {
        background = this@SKIN.background
    }

    val solidFrameTextureSelected by lazy {
        Texture2D {
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
            image = Image(4, 4) {
                val b = 0xFF8000FF.toInt()
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

    val solidFrameSelected = NinePatch(solidFrameTextureSelected, 1, 1, 1, 1)

    val checkBox = CheckBoxStyle(null, null)
}
