package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.ui.*

abstract class ComponentScenePanel<C: IEntityComponent, T>: IComponentScenePanel<C> {
    override val componentSceneContent: Actor
        get() = rootSceneStack

    override var component: C? = null

    val listPanel = UIList<T>()

    val itemPanelTable = Table()
    val itemPanelScroll = ScrollPane(style = SKIN.scroll)

    val split = MultiSplitPane(false)

    val rootSceneStack = Stack {
        touchable = Touchable.ChildrenOnly
        add(split)
    }

    init {
        itemPanelScroll.actor = itemPanelTable
        //sceneOverlay.touchable = Touchable.ChildrenOnly
    }

    fun setupScene(scene: Actor, overlay: Actor) {
        rootSceneStack.clearChildren()
        rootSceneStack.add(scene)
        rootSceneStack.add(split)

        split.apply {
            setWidgets(listPanel, overlay, itemPanelScroll)
            setSplit(0, 0.2f)
            setSplit(1, 0.8f)
        }
    }
}