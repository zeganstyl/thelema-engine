package app.thelema.studio.tool

import app.thelema.app.APP
import app.thelema.g2d.Batch
import app.thelema.studio.SKIN
import app.thelema.ui.*

class TerminalDialog: Window("") {
    val textContent = Label()

    val scroll = ScrollPane(textContent, style = SKIN.scroll)

    var output: (out: StringBuilder) -> Boolean = { false }

    val stringBuilder = StringBuilder()

    var openBuildDir: () -> Unit = {}

    init {
        textContent.alignV = 1
        textContent.alignH = -1
        textContent.lineAlign = -1
        textContent.setWrap(true)

        content.align = Align.topLeft
        content.add(scroll).grow().align(Align.topLeft).newRow()
        content.add(HBox {

            add(TextButton("Copy") {
                onClick {
                    APP.clipboardString = textContent.text.toString()
                }
            })

            add(TextButton("Open build directory") {
                onClick {
                    openBuildDir()
                    openBuildDir = {}
                }
            }).padLeft(20f)

        }).padTop(20f)

        pack()
        width = 640f
        height = 480f
        isResizable = true
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        if (output(stringBuilder)) {
            textContent.text = stringBuilder.toString().also { println(it) }
        }
    }
}