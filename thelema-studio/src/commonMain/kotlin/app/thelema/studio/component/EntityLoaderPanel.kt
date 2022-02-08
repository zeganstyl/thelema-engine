package app.thelema.studio.component

import app.thelema.ecs.EntityLoader
import app.thelema.studio.Studio
import app.thelema.ui.InputEvent
import app.thelema.ui.MenuItem
import app.thelema.ui.TextButton

class EntityLoaderPanel: ComponentPanel<EntityLoader>(EntityLoader::class) {

    override val menuItems: List<MenuItem> = listOf(
        MenuItem("Open Scene") { onClick(::openScene) }
    )

    init {
        content.add(TextButton("Open Scene") { onClick(::openScene) }).newRow()
    }

    fun openScene(event: InputEvent) {
        component?.also { Studio.openEntity(it) }
    }
}