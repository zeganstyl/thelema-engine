package app.thelema.studio.component

import app.thelema.audio.ISound
import app.thelema.ui.TextButton

class SoundPanel: ComponentPanel<ISound>(ISound::class) {
    init {
        content.add(TextButton("Play") {
            onClick {
                component?.also { it.playSound() }
            }
        }).newRow()
    }
}
