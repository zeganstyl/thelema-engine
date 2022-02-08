package app.thelema.studio.component

import app.thelema.ecs.IEntityComponent
import app.thelema.studio.SKIN
import app.thelema.ui.*

abstract class ComponentScenePanel<C: IEntityComponent, T>: IComponentScenePanel<C> {
    override val componentSceneContent: Actor
        get() = rootSceneStack

    override var component: C? = null

    val listPanel = UIList<T>(style = SKIN.list)
    val listScroll = ScrollPane(listPanel, style = SKIN.scroll)

    val itemPanelTable = Table()
    val itemPanelScroll = ScrollPane(style = SKIN.scroll)

    val split = MultiSplitPane(false)

    val rootSceneStack = Stack {
        touchable = Touchable.ChildrenOnly
        add(split)
    }

    init {
        itemPanelTable.align = Align.topLeft
        itemPanelScroll.actor = itemPanelTable
        //sceneOverlay.touchable = Touchable.ChildrenOnly
    }

    fun setupScene(scene: Actor, overlay: Actor, underlay: Actor? = null) {
        rootSceneStack.clearChildren()
        underlay?.also { rootSceneStack.add(it) }
        rootSceneStack.add(scene)
        rootSceneStack.add(split)

        split.apply {
            setWidgets(listScroll, overlay, itemPanelScroll)
            setSplit(0, 0.2f)
            setSplit(1, 0.8f)
        }
    }
}