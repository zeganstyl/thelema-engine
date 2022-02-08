package app.thelema.studio.field

import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.studio.Studio
import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.studio.ecs.KotlinScripting
import app.thelema.ui.*

class ScriptFileField: Table(), PropertyProvider<IFile?> {
    val textField = TextField()
    val chooseButton = TextButton("...") {
        onClick {
            if (KotlinScripting.kotlinDirectory == null) {
                Studio.showStatusAlert("Kotlin directory is not set or project is not opened")
            } else {
                Studio.fileChooser.openScriptFile(get()) {
                    if (it != null) {
                        value = it.path
                        set(it)
                    }
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
                        set(FS.file(textField.text, FileLocation.Relative))
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