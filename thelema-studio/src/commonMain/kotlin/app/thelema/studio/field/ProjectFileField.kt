package app.thelema.studio.field

import app.thelema.fs.DefaultProjectFile
import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.studio.Studio
import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.res.RES
import app.thelema.ui.*

class ProjectFileField: Table(), PropertyProvider<IFile?> {
    val textField = TextField()
    val chooseButton = TextButton("...") {
        onClick {
            if (RES.file != DefaultProjectFile && RES.file.exists()) {
                Studio.fileChooser.openProjectFile(get()) {
                    value = it
                    set(FS.file(it, FileLocation.Project))
                }
            }
        }
    }

    override var set: (value: IFile?) -> Unit = {}

    override var get: () -> IFile? = { null }

    var value: String = ""
        set(value) {
            if (value != field) {
                field = value
                textField.text = value
            }
        }

    init {
        textField.addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> {
                        set(FS.file(textField.text, FileLocation.Project))
                    }
                }
                return super.keyDown(event, keycode)
            }
        })

        add(textField).growX()
        add(chooseButton).width(20f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!textField.focused) {
            value = get()?.path ?: ""
        }
        super.draw(batch, parentAlpha)
    }
}