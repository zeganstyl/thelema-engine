package app.thelema.studio.widget.component

import app.thelema.studio.KotlinScriptStudio
import app.thelema.studio.KotlinScripting
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class KotlinScriptPanel: ComponentPanel<KotlinScriptStudio>("KotlinScript") {
    init {
        content.add(TextButton("execute") {
            onClick {
                component?.execute()
            }
        }).newRow()

        content.add(TextButton("Open directory") {
            onClick {
                KotlinScripting.kotlinDirectory?.also { kotlinDir ->
                    component?.file?.also { Studio.fileChooser.openInFileManager("${kotlinDir.platformPath}/${it.parent().platformPath}") }
                }
            }
        }).newRow()
    }
}