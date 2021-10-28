package app.thelema.studio.widget.component

import app.thelema.ecs.EntityLoader
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class EntityLoaderPanel: ComponentPanel<EntityLoader>(componentName<EntityLoader>()) {
    init {
        content.add(TextButton("Open scene") {
            onClick {
                component?.also { Studio.openEntity(it) }
            }
        }).newRow()
    }
}