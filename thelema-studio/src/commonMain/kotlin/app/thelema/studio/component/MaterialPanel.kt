package app.thelema.studio.component

import app.thelema.data.DATA
import app.thelema.ecs.DefaultComponentSystemLayer
import app.thelema.ecs.Entity
import app.thelema.g2d.Sprite
import app.thelema.g3d.IMaterial
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.mesh.sphereMesh
import app.thelema.g3d.scene
import app.thelema.g3d.simpleSkybox
import app.thelema.gl.GL
import app.thelema.img.*
import app.thelema.math.Vec3
import app.thelema.ui.Scaling
import app.thelema.ui.UIImage
import app.thelema.utils.iterate

class MaterialPanel: ComponentPanel<IMaterial>(IMaterial::class) {
    val sprite = Sprite()

    override var component: IMaterial?
        get() = super.component
        set(value) {
            super.component = value
            MaterialPreview.sphere.mesh.material = value
            sprite.texture = MaterialPreview.fb.texture as ITexture2D
        }

    val image = UIImage(sprite)

    var renderPreview = false

    init {
        image.scaling = Scaling.fit
        content.add(image).growX().height(200f).newRow()

        GL.render {
            if (MaterialPreview.initOnGLThreadRequest) MaterialPreview.initOnGLThread()

            if (renderPreview) {
                MaterialPreview.fb.render {
                    MaterialPreview.system.render()
                }
            }
        }
    }

    override fun act(delta: Float) {
        renderPreview = true
        MaterialPreview.system.update(delta)
        MaterialPreview.camera.also {
            if (it.viewportWidth != image.imageWidth || it.viewportHeight != image.imageHeight) {
                it.viewportWidth = image.imageWidth
                it.viewportHeight = image.imageHeight
                it.updateCamera()
            }
        }

        super.act(delta)
    }
}

object MaterialPreview {
    val fb = SimpleFrameBuffer(width = 512, height = 512)

    val system = DefaultComponentSystemLayer()

    val camera = Camera {
        lookAt(Vec3(0f, 0f, 2.5f), Vec3(0f))
    }
    val previewScene = Entity("preview") {
        scene {
            activeCamera = camera
        }
        entity("light") {
            directionalLight {
                setDirectionFromPosition(0f, 0f, 1f)
            }
        }
    }
    val sphere = previewScene.entity("sphere").sphereMesh {
        builder.normals = true
        builder.uvs = true
        builder.tangents = true
        setSize(1f)
    }
    val defaultSkyboxTexture = TextureCube()
    val skybox = previewScene.entity("skybox").simpleSkybox {
        texture = defaultSkyboxTexture
    }

    var initOnGLThreadRequest = true

    init {
        system.addedScene(previewScene)
    }

    fun initOnGLThread() {
        initOnGLThreadRequest = false

        val bytes = DATA.bytes(4 * 4 * 4) {
            val b = 0x000000FF
            val f = 0x808080FF.toInt()
            putRGBAs(
                b, b, b, b,
                b, f, f, b,
                b, f, f, b,
                b, b, b, b
            )
            rewind()
        }
        defaultSkyboxTexture.sides.iterate {
            it.load(4, 4, bytes, 0)
        }
        bytes.destroy()
    }
}
