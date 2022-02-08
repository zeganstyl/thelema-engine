package app.thelema.studio.tool

import app.thelema.ui.*

class MoveOrCopyDialog: Window("Move or Copy?") {
    var onCopy: () -> Unit = {}
    var onMove: () -> Unit = {}

    init {
        content.add(Label("Move or copy selected files?")).newRow()
        content.add(HBox {
            add(TextButton("Copy") {
                onClick {
                    onCopy()
                    onCopy = {}
                    hide()
                }
            })
            add(TextButton("Move") {
                onClick {
                    onMove()
                    onMove = {}
                    hide()
                }
            }).padLeft(20f)
        }).padTop(20f)

        pack()
    }
}