package app.thelema.studio.widget.component

import app.thelema.gltf.GLTF
import app.thelema.gltf.GLTFScene
import app.thelema.studio.Studio
import app.thelema.ui.InputEvent
import app.thelema.ui.MenuItem
import app.thelema.ui.TextButton
import app.thelema.utils.LOG

class GLTFPanel: ComponentPanel<GLTF>(GLTF::class) {
    override val menuItems: List<MenuItem> = listOf(
        MenuItem("Open Main Scene") { onClick(::openMainScene) }
    )

    init {
        content.add(TextButton("Open Main Scene") { onClick(::openMainScene) }).newRow()
    }

    fun openMainScene(event: InputEvent) {
        val component = component ?: return
        if (component.isLoaded) {
            component.scenes.getOrNullTyped<GLTFScene>(component.mainSceneIndex)?.loader?.also {
                Studio.openEntity(it)
            }
        } else {
            val file = component.file
            if (file == null || !file.exists()) {
                Studio.showStatusAlert("File is not exists: ${file?.path}")
            } else {
                component.onLoaded {
                    component.scenes.getOrNullTyped<GLTFScene>(component.mainSceneIndex)?.loader?.also {
                        Studio.openEntity(it)
                    }
                }
                component.load()
            }
        }
    }
}