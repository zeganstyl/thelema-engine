package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.studio.widget.component.*

object ComponentPanelProvider {
    val providers = HashMap<String, () -> ComponentPanel<IEntityComponent>>()

    val componentPanelsCache = HashMap<String, ComponentPanel<IEntityComponent>>()

    fun init() {
        addProvider { TransformNodePanel() }
        addProvider { ProjectPanel() }
        addProvider { LoaderPanel() }
        addProvider { EntityLoaderPanel() }
        addProvider { ScenePanel() }
        addProvider { ShaderPanel() }
        addProvider { Texture2DPanel() }
        addProvider { MaterialPanel() }
        addProvider { MeshPanel() }
        addProvider { AnimationPlayerPanel() }
        addProvider { ArmaturePanel() }
        addProvider("KotlinScript") { KotlinScriptPanel() }
    }

    fun getOrCreatePanel(componentName: String): ComponentPanel<IEntityComponent> {
        var panel = componentPanelsCache[componentName]
        if (panel == null) {
            panel = providers[componentName]?.invoke() ?: ComponentPanel(componentName)
            componentPanelsCache[componentName] = panel
        }
        return panel
    }

    fun getOrCreatePanel(component: IEntityComponent): ComponentPanel<IEntityComponent> =
        getOrCreatePanel(component.componentName).also { it.component = component }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: IEntityComponent> addProvider(noinline create: () -> ComponentPanel<T>) {
        providers[ComponentPanel.componentName<T>()] = create as () -> ComponentPanel<IEntityComponent>
    }

    fun <T: IEntityComponent> addProvider(componentName: String, create: () -> ComponentPanel<T>) {
        providers[componentName] = create as () -> ComponentPanel<IEntityComponent>
    }
}
