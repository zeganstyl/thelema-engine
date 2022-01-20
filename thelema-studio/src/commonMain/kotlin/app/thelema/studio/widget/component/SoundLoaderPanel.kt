package app.thelema.studio.widget.component

import app.thelema.audio.ISoundLoader
import app.thelema.g3d.IScene
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class SoundLoaderPanel: ComponentPanel<ISoundLoader>(ISoundLoader::class) {
    init {
        content.add(TextButton("Play") {
            onClick {
                component?.also {
                    if (!it.isLoaded) it.load() else it.play()
                }
            }
        }).newRow()

        content.add(TextButton("Stop") {
            onClick {
                component?.also { it.stopSound() }
            }
        }).newRow()
    }
}
