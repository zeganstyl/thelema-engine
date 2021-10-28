package app.thelema.studio.widget.component

import app.thelema.studio.KotlinScriptStudio
import app.thelema.studio.KotlinScripting
import app.thelema.ui.TextButton

class KotlinScriptPanel: ComponentPanel<KotlinScriptStudio>("KotlinScript") {
    init {
        content.add(TextButton("execute") {
            onClick {
                component?.execute()
                KotlinScripting.bakeScripts()
            }
        }).newRow()
    }
}