package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.studio.widget.component.*

object ComponentPanelProvider {
    val providers = HashMap<String, () -> ComponentPanel<IEntityComponent>>()

    fun init() {
        addProvider { TransformNodePanel() }
        addProvider { ProjectPanel() }
        addProvider { LoaderPanel() }
        addProvider { EntityLoaderPanel() }
        addProvider { ScenePanel() }
        addProvider { ShaderPanel() }
        addProvider { Texture2DPanel() }
        addProvider { MaterialPanel() }
        addProvider("KotlinScript") { KotlinScriptPanel() }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: IEntityComponent> addProvider(noinline create: () -> ComponentPanel<T>) {
        providers[ComponentPanel.componentName<T>()] = create as () -> ComponentPanel<IEntityComponent>
    }

    fun <T: IEntityComponent> addProvider(componentName: String, create: () -> ComponentPanel<T>) {
        providers[componentName] = create as () -> ComponentPanel<IEntityComponent>
    }
}

class Person {
    var name: String = "Hello"
    var surname: String = "world"

    var fullName: String
        get() = "$name $surname"
        set(value) {
            value.split(' ').also {
                name = it[0]
                surname = it[1]
            }
        }
}