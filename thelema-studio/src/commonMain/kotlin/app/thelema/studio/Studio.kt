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
import app.thelema.studio.widget.component.ProjectPanel
import app.thelema.ui.*
import app.thelema.utils.Color
import app.thelema.utils.iterate

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

    val createProjectWindow = CreateProjectWindow()

    val chooseComponentWindow = ChooseComponentWindow()

    private val prefsName = "thelema-studio"

    val popupMenu = PopupMenu {
        item("Add Entity") {
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.addEntity(Entity("New Entity"))
                it.isExpanded = true
            }
        }
        separator()
        item("Edit") {
            onClickWithContextTyped<EntityTreeNode> { it.showEditWindow() }
        }
        separator()
        item("Remove") {
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

    var appProjectDirectory: IFile? = null

    init {
        APP.setupPhysicsComponents()

        ECS.replaceDescriptor(KotlinScript::class.simpleName!!, { KotlinScriptStudio() }) {
            setAliases(IKotlinScript::class)
            string("customMainFunctionName", { customMainFunctionName }) { customMainFunctionName = it }
            kotlinScriptFile("file", { file }) { file = it }
        }

        ComponentPanelProvider.init()

        APP.addListener(this)

        ECS.addSystem(StudioComponentSystem(hud))

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
        openThelemaApp(file)

        // src
        //  - kotlin
        //  - resources/app.thelema
        val kotlinSiblingDir = if (file.isDirectory) file.parent().child("kotlin") else file.parent().parent().child("kotlin")
        KotlinScripting.kotlinDirectory = if (kotlinSiblingDir.exists()) kotlinSiblingDir else file("")

        // project/src/main/kotlin
        appProjectDirectory = kotlinSiblingDir.parent().parent().parent()

        projectTab = EntityTab(RES.entity, null)

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

    fun createNewApp(script: IFile? = null) {
        tabsPane.clearTabs()
        tabsPane.addTab(projectTab)
        RES.destroy()
        RES.file = DefaultProjectFile
        createNewScene(script)
    }

    fun createNewProject() {
        createProjectWindow.show(hud)
    }

    fun createNewScene(script: IFile? = null) {
        val sceneName = RES.entity.makeChildName("NewScene")
        val entity = Entity(sceneName)
        RES.entity.addEntity(entity)
        entity.apply {
            RES.mainScene = entityLoader {
                targetEntity.apply {
                    this.name = sceneName
                    scene()

                    if (script != null) {
                        component<IKotlinScript> {
                            this as KotlinScriptStudio
                            this.file = script
                        }
                    }

                    entity("Light") {
                        directionalLight {
                            setDirectionFromPosition(1f, 1f, 1f)
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

    fun saveApp(file: IFile) {
        RES.file = if (file.isDirectory) file.child(APP_ROOT_FILENAME) else file
        RES.file.writeText(JSON.printObject(RES.entity))
        savePreferences()
    }

    fun saveApp() {
        if (RES.file == DefaultProjectFile) {
            fileChooser.saveProject {
                val file = FS.absolute(it)
                saveApp(file)
                showStatus("App saved")
            }
        } else {
            RES.file.writeText(JSON.printObject(RES.entity))
            savePreferences()
            showStatus("App saved")
        }
    }

    override fun update(delta: Float) {
        hud.update(delta)
        CameraControl.control.update(delta)
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

    override fun filesDropped(files: List<IFile>) {
        if (RES.file != DefaultProjectFile) {
            val dialog = MoveOrCopyDialog()
            dialog.onMove = {
                files.iterate {
                    it.moveTo(RES.absoluteDirectory.child(it.name))
                }
                ProjectPanel.findResources()
            }
            dialog.onCopy = {
                files.iterate {
                    if (it.isDirectory)
                        it.copyTo(RES.absoluteDirectory)
                    else
                        it.copyTo(RES.absoluteDirectory.child(it.name))
                }
                ProjectPanel.findResources()
            }
            dialog.show(hud)
        }
    }

    override fun destroy() {
        fileChooser.destroy()
    }
}
