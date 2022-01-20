package app.thelema.audio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.g3d.TransformNodeListener
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.math.Vec3

class Sound3D: IEntityComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override val componentName: String
        get() = "Sound3D"

    var id: Int = -1

    val tmp = Vec3()
    val tmp2 = Vec3()

    val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            loader?.setPan(id, getPan())
            loader?.setVolume(id, volume * getAttenuation())
        }
    }

    var loader: ISoundLoader? = null
        set(value) {
            if (field != value) {
                field = value
                value?.load()
            }
        }

    var node: ITransformNode = TransformNode().apply { addListener(nodeListener) }
        set(value) {
            if (field != value) {
                field = value
                value.addListener(nodeListener)
            }
        }

    var pitch: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                loader?.setPitch(id, value)
            }
        }

    var volume: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                loader?.setVolume(id, value)
            }
        }

    var isLooped: Boolean = false

    fun getPan(): Float {
        val mat = ActiveCamera.node.worldMatrix
        val soundMat = node.worldMatrix

        soundMat.getCol0Vec3(tmp).sub(mat.m03, mat.m13, mat.m23)
        mat.getCol0Vec3(tmp2)
        tmp.nor()
        return tmp.dot(tmp2).also { println(it) }
    }

    fun getAttenuation(): Float {
        val mat = ActiveCamera.node.worldMatrix
        val soundMat = node.worldMatrix

        return 1f / soundMat.getCol3Vec3(tmp).dst(mat.m03, mat.m13, mat.m23)
    }

    fun play() {
        val attenuation = getAttenuation()
        if (attenuation > 0.001) {
            id = loader?.play(volume * attenuation, pitch, getPan(), isLooped) ?: -1
        }
    }
}