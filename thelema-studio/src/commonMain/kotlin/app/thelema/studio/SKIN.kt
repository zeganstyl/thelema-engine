package app.thelema.studio

import app.thelema.g2d.Sprite
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
}
