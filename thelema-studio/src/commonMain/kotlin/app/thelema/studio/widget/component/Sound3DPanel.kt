package app.thelema.studio.widget.component

import app.thelema.audio.Sound3D
import app.thelema.ui.TextButton

class Sound3DPanel: ComponentPanel<Sound3D>(Sound3D::class) {
    init {
        content.add(TextButton("Play") {
            onClick {
                component?.also { it.play() }
            }
        }).newRow()
    }
}
