package app.thelema.studio.ecs

import app.thelema.ecs.*
import app.thelema.g2d.Sprite
import app.thelema.g3d.ISceneProvider
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.img.texture2D
import app.thelema.input.BUTTON
import app.thelema.res.AID
import app.thelema.res.RES
import app.thelema.studio.SKIN
import app.thelema.studio.Studio
import app.thelema.studio.component.ComponentsPanel
import app.thelema.studio.g3d.Selection3D
import app.thelema.studio.widget.Tab
import app.thelema.studio.widget.TabsPane
import app.thelema.ui.*

class MainTab: Tab {
    override val tabPanel: Actor
        get() = root

    override val tabTitleLabel: Label = Label("App")

    override val tabTitle: Table = Table {
        pad(5f)
        add(tabTitleLabel).padLeft(5f).padRight(5f)
    }

    val scenes = ArrayList<EntityTreeNode>()
    val scenesList = UIList<EntityTreeNode> {
        style = SKIN.listEmpty
        items = scenes
        this.itemToString = { it.entity.pathWithoutRes }
    }

    val componentsPanel = ComponentsPanel()

    val scenesPanel = VBox {
        align = Align.topLeft
        add(Label("Scenes")).pad(10f)
        add(ScrollPane(scenesList, style = SKIN.scrollEmpty)).pad(10f)
    }
    val split = MultiSplitPane(false, style = SKIN.split) {
        setWidgets(Studio.projectTree, scenesPanel, componentsPanel)
    }

    val root = Stack {
        add(Table().apply {
            background = SKIN.background
            add(split).grow()
        })
    }

    override var tabsPaneOrNull: TabsPane<Tab>? = null

    override val tabCloseButton: Actor?
        get() = null

    private val projectSelectionListener = object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            componentsPanel.entity = Studio.projectTree.selected?.entity
        }
    }

    init {
        root.fillParent = true

        scenesList.addListener(object : ClickListener(BUTTON.LEFT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                componentsPanel.entity = scenesList.selection.lastSelected?.entity
            }
        })
        scenesList.addListener(object : ClickListener(BUTTON.RIGHT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                Studio.popupMenu.showMenu(event, scenesList.selection.lastSelected)
            }
        })

        RES.entity.forEachComponentInBranch { scene ->
            if (scene is ISceneProvider) scenes.add(EntityTreeNode(scene.entity))
        }

        tabTitle.touchable = Touchable.Enabled
        tabTitle.addListener(object : InputListener {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                tabTitle.background = SKIN.overBackground
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                tabTitle.background = if (tabsPane.activeTab == this@MainTab) SKIN.titleBackground else null
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
            split.setWidgets(Studio.projectTree, scenesPanel, componentsPanel)
        }

        tabTitle.background = if (activated) SKIN.titleBackground else null

        if (newTab == this) {
            Selection3D.selection = null
            Studio.projectTree.tree.addListener(projectSelectionListener)
        } else {
            Studio.projectTree.tree.removeListener(projectSelectionListener)
        }
    }
}

val IEntity.pathWithoutRes: String
    get() {
        val parentPath = parentEntity ?: return ""
        return if (parentEntity == RES.entity) name else {
            var path = parentPath.pathWithoutRes
            if (path.isNotEmpty()) path += IEntity.Companion.delimiter
            path + name
        }
    }