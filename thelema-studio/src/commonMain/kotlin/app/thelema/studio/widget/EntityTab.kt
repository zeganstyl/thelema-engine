package app.thelema.studio.widget

import app.thelema.ecs.ECS
import app.thelema.ecs.EntityLoader
import app.thelema.ecs.IEntity
import app.thelema.studio.*
import app.thelema.studio.Tab
import app.thelema.ui.*

open class EntityTab(entity: IEntity, tabCloseButton: TextButton? = TextButton("X")): Tab {
    override val tabPanel: Actor
        get() = scene

    override val tabTitleLabel: Label = Label().apply {
        textProvider = { scene.entity.name }
    }

    override val tabTitle: Table = Table {
        pad(5f)
        add(tabTitleLabel).padLeft(5f).padRight(5f)
        if (tabCloseButton != null) add(tabCloseButton).padRight(5f)
    }

    val scene = TabScenePanel()

    override var tabsPaneOrNull: TabsPane<Tab>? = null

    override val tabCloseButton: Actor? = tabCloseButton

    var loader: EntityLoader? = null

    init {
        scene.entity = entity

        tabTitle.touchable = Touchable.Enabled
        tabTitle.addListener(object : InputListener {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                tabTitle.background = SKIN.overBackground
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                tabTitle.background = if (tabsPane.activeTab == this@EntityTab) SKIN.titleBackground else null
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (event.target == tabCloseButton) {
                    event.target?.also { it.hud?.unfocus(it) }
                    removeTab()
                } else {
                    makeTabActive()
                }
            }
        })
    }

    override fun tabSwitched(activated: Boolean, oldTab: Tab?, newTab: Tab?) {
        if (activated) {
            ECS.addEntity(scene.entity)
        } else {
            ECS.removeEntity(scene.entity)
        }

        tabTitle.background = if (activated) SKIN.titleBackground else null

        Selection3D.selection = (newTab as EntityTab?)?.scene?.selection
    }
}