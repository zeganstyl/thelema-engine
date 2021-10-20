package app.thelema.studio

import app.thelema.studio.widget.EntityTab
import app.thelema.studio.widget.MenuBar
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
import app.thelema.math.Mat4
import app.thelema.res.*
import app.thelema.shader.PBRShader
import app.thelema.shader.SimpleShader3D
import app.thelema.ui.*

object Studio: AppListener, IJsonObjectIO {
    init {
        SKIN.init()
        APP.setupPhysicsComponents()
    }

    val hud = HeadUpDisplay()

    lateinit var fileChooser: IFileChooser

    val projectTab = EntityTab(RES.entity, null)

    val menuBar = MenuBar()

    val statusBar = Table {
        background = SKIN.background
    }
    val statusLabel = Label("")

    val tabsPane = TabsPane<EntityTab>().apply {
        addTab(projectTab)
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
        add(tabsPane.tabContent).grow()
        add(UIImage(SKIN.hLine)).growX().height(1f)
        add(statusBar).growX()
    }

    init {
        ComponentPanelProvider.init()

        statusBar.add(statusLabel)
        statusBar.align = Align.left

        hud.addActor(root)

        APP.addListener(this)

        KB.addListener(hud)
        MOUSE.addListener(hud)

        RES.loadOnSeparateThreadByDefault = true

        openProjectTab()
        createNewScene()

        ActiveCamera {
            setNearFar(0.01f, 1000f)
        }
    }

    fun openProjectDialog() {
        fileChooser.openProject {
            // TODO
        }
    }

    fun createNewScene() {
        val sceneName = RES.entity.makeChildName("NewScene")
        val fileName = sceneName + EntityLoader.ext
        val entity = Entity(sceneName)
        RES.entity.addEntity(entity)
        entity.apply {
            entityLoader {
                file = projectFile(fileName)
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
                        boxMesh(1f)
                        material {
                            shader = PBRShader {
                                setBaseColorTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Color.jpg")
                                setAlphaTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Opacity.jpg")
                                setNormalTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Normal.jpg")
                                setRoughnessTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Roughness.jpg")
                                setMetallicTexture("MetalWalkway012_1K-JPG/MetalWalkway012_1K_Metalness.jpg")

                                outputNode.cullFaceMode = 0
                                outputNode.alphaMode = Blending.MASK

                                baseColorTextureNode.sRGB = false
                                normalTextureNode.sRGB = false
                                metallicTextureNode.sRGB = false
                                roughnessTextureNode.sRGB = false
                                alphaTextureNode.sRGB = false
                            }
                        }
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

    override fun readJson(json: IJsonObject) {}

    override fun writeJson(json: IJsonObject) {}

    fun saveProject() {
        RES.file.also { projectFile ->
            projectFile.writeText(JSON.printObject(RES.entity))
        }
    }

    override fun update(delta: Float) {
        hud.update(delta)
        CameraControl.control.update(delta)
    }

    override fun render() {
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
