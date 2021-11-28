package app.thelema.studio

import app.thelema.studio.widget.EntityTab
import app.thelema.studio.widget.StudioMenuBar
import app.thelema.app.APP
import app.thelema.app.AppListener
import app.thelema.ecs.*
import app.thelema.fs.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.*
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.mesh.boxMesh
import app.thelema.input.MOUSE
import app.thelema.input.KB
import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO
import app.thelema.json.JSON
import app.thelema.res.*
import app.thelema.script.IKotlinScript
import app.thelema.script.KotlinScript
import app.thelema.studio.widget.SimulationEntityTab
import app.thelema.ui.*
import app.thelema.utils.Color

object Studio: AppListener, IJsonObjectIO {
    lateinit var fileChooser: IFileChooser

    var projectTab = EntityTab(RES.entity, null)

    val menuBar = StudioMenuBar()

    val statusLabel = Label("")
    val statusBar = Table {
        background = SKIN.background
        add(statusLabel)
        align = Align.left
    }

    val tabsPane = TabsPane<EntityTab>().apply {
        addTab(projectTab)
    }

    val scenesSplit = MultiSplitPane( true) {
        setWidgets(tabsPane.tabContent)
    }

    val root = VBox {
        fillParent = true
        add(menuBar).growX()
        add(UIImage(SKIN.hLine)).growX().height(1f)
        add(HBox {
            background = SKIN.background
            add(tabsPane.titleBar).growX()
        }).growX()
        add(UIImage(SKIN.hLine)).growX().height(1f)
        add(scenesSplit).grow()
        add(UIImage(SKIN.hLine)).growX().height(1f)
        add(statusBar).growX()
    }

    val hud = HeadUpDisplay().apply {
        addActor(Studio.root)
        KB.addListener(this)
        MOUSE.addListener(this)
    }

    val entityWindow = EntityWindow()

    val entityTreeWindow = EntityTreeWindow()

    private val prefsName = "thelema-studio"

    val popupMenu = PopupMenu {
        item("Add Entity") {
            label.alignH = -1
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.addEntity(Entity("New Entity"))
                it.isExpanded = true
            }
        }
        separator()
        item("Edit") {
            label.alignH = -1
            onClickWithContextTyped<EntityTreeNode> {
                entityWindow.entity = it.entity
                entityWindow.show(hud)
            }
        }
        separator()
        item("Remove") {
            label.alignH = -1
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.parentEntity?.removeEntity(it.entity)
            }
        }
    }

    var componentScenePanel: IComponentScenePanel<IEntityComponent>? = null
        set(value) {
            field = value
            val split = scenesSplit.splits.getOrNull(0) ?: 1f
            if (value != null) {
                scenesSplit.setWidgets(tabsPane.tabContent, value.componentSceneContent)
                scenesSplit.setSplit(0, split)
            } else {
                scenesSplit.setWidgets(tabsPane.tabContent)
            }
        }

    init {
        APP.setupPhysicsComponents()

        ECS.replaceDescriptor(KotlinScript::class.simpleName!!, { KotlinScriptStudio() }) {
            setAliases(IKotlinScript::class)
            string("customMainFunctionName", { customMainFunctionName }) { customMainFunctionName = it }
            kotlinScriptFile("file", { file }) { file = it }
        }

        ComponentPanelProvider.init()

        APP.addListener(this)

        RES.loadOnSeparateThreadByDefault = true

        openProjectTab()

        val str = APP.loadPreferences(prefsName)
        if (str.isNotEmpty()) {
            readJson(JSON.parseObject(str))
        } else {
            createNewScene()
        }

        ActiveCamera {
            setNearFar(0.01f, 1000f)
        }
    }

    fun showStatus(text: String, color: Int = Color.WHITE) {
        statusLabel.text = text
        statusLabel.color = color
    }

    fun showStatusAlert(text: String, color: Int = Color.RED) {
        statusLabel.text = text
        statusLabel.color = color
    }

    fun openProject(file: IFile) {
        openThelemaProject(file)

        val kotlinSiblingDir = if (file.isDirectory) file.parent().child("kotlin") else file.parent().parent().child("kotlin")
        KotlinScripting.kotlinDirectory = if (kotlinSiblingDir.exists()) kotlinSiblingDir else file("")

        projectTab = EntityTab(RES.entity, null)
        menuBar.projectPath.text = RES.file.path

        ECS.removeAllEntities()
        tabsPane.clearTabs()
        tabsPane.addTab(projectTab)
        openProjectTab()
    }

    fun startSimulation(source: IEntity) {
        val tab = SimulationEntityTab(source)
        tabsPane.addTab(tab, true)
        tab.startSimulation()
    }

    fun openProjectDialog() {
        fileChooser.openProject {
            openProject(FS.absolute(it))
            savePreferences()
        }
    }

    fun createNewProject() {
        RES.destroy()
        RES.file = DefaultProjectFile
        createNewScene()
    }

    fun createNewScene() {
        val sceneName = RES.entity.makeChildName("NewScene")
        val entity = Entity(sceneName)
        RES.entity.addEntity(entity)
        entity.apply {
            entityLoader {
                targetEntity.apply {
                    this.name = sceneName
                    scene()

                    entity("Light") {
                        directionalLight {
                            setDirectionFromPosition(1f, 0f, 1f)
                            intensity = 5f
                        }
                    }
                    entity("Box") {
                        boxMesh()
                        material()
                        transformNode()
                    }
                }

                openEntity(this)
            }
        }
    }

    fun openEntity(loader: EntityLoader) {
        loader.load()
        tabsPane.activeTab = tabsPane.tabs.firstOrNull { it.loader == loader } ?: EntityTab(loader.targetEntity).also {
            it.loader = loader
            tabsPane.addTab(it, false)
        }
    }

    fun openProjectTab() {
        tabsPane.activeTab = projectTab
    }

    override fun readJson(json: IJsonObject) {
        json.string("lastProject") {
            val file = FS.absolute(it)
            if (file.exists()) openProject(file)
        }

        json.array("openedScenes") {}
        json.string("lastScene") {}

        if (json.string("lastProject", "").isEmpty()) createNewScene()
    }

    override fun writeJson(json: IJsonObject) {
        val lastProject = RES.file.path
        if (lastProject.isNotEmpty()) json["lastProject"] = lastProject
    }

    fun savePreferences() {
        APP.savePreferences(prefsName, JSON.printObject(this))
    }

    fun saveProject() {
        if (RES.file == DefaultProjectFile) {
            fileChooser.saveProject {
                val file = FS.absolute(it)
                RES.file = if (file.isDirectory) file.child(PROJECT_FILENAME) else file
                RES.file.writeText(JSON.printObject(RES.entity))
                savePreferences()
                showStatus("Project saved")
            }
        } else {
            RES.file.writeText(JSON.printObject(RES.entity))
            savePreferences()
            showStatus("Project saved")
        }
    }

    override fun update(delta: Float) {
        hud.update(delta)
        CameraControl.control.update(delta)
    }

    override fun render() {
        Selection3D.prepareSelection()
        Selection3D.render()
        hud.render()
    }

    override fun resized(width: Int, height: Int) {
        hud.camera.viewportWidth = width.toFloat()
        hud.camera.viewportHeight = height.toFloat()
        hud.camera.updateCamera()
        ActiveCamera.viewportWidth = width.toFloat()
        ActiveCamera.viewportHeight = height.toFloat()
        ActiveCamera.aspectRatio = width.toFloat() / height.toFloat()
        ActiveCamera.updateCamera()
    }

    override fun destroy() {
        fileChooser.destroy()
    }
}
