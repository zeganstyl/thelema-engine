package app.thelema.lwjgl3.audio

import app.thelema.audio.IAudioListener
import app.thelema.ecs.IEntity
import app.thelema.ecs.UpdatableComponent
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNodeListener
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.math.Vec3
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import java.nio.FloatBuffer

class AudioListener: IAudioListener {
    override var entityOrNull: IEntity? = null

    private val orientation = BufferUtils.createFloatBuffer(6)
        .put(floatArrayOf(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)).flip() as FloatBuffer

    private val position = BufferUtils.createFloatBuffer(3)
        .put(floatArrayOf(0.0f, 0.0f, 0.0f)).flip() as FloatBuffer

    private val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            transformUpdated()
        }
    }

    override var node: ITransformNode? = null
        set(value) {
            if (field != value) {
                field?.removeListener(nodeListener)
                field = value
                transformUpdated()
                value?.addListener(nodeListener)
            }
        }

    override var gain: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                AL10.alListenerf(AL10.AL_GAIN, value)
            }
        }

    override var useActiveCamera: Boolean = true

    private val tmp = Vec3()

    override var isEnabled: Boolean = true

    override fun updateListener(delta: Float) {
        if (useActiveCamera) node = ActiveCamera.node
    }

    private fun transformUpdated() {
        if (useActiveCamera) {
            ActiveCamera.viewMatrix.getRow2Vec3(tmp).nor().scl(-1f)
            orientation.put(0, tmp.x)
            orientation.put(1, tmp.y)
            orientation.put(2, tmp.z)
            ActiveCamera.viewMatrix.getRow1Vec3(tmp).nor()
            orientation.put(3, tmp.x)
            orientation.put(4, tmp.y)
            orientation.put(5, tmp.z)
            AL10.alListenerfv(AL10.AL_ORIENTATION, orientation)

            tmp.set(ActiveCamera.node.worldPosition)
            position.put(0, tmp.x)
            position.put(1, tmp.y)
            position.put(2, tmp.z)
            AL10.alListenerfv(AL10.AL_POSITION, position)
        } else {
            val node = node
            if (node != null) {
                node.getDirection(tmp).scl(-1f)
                orientation.put(0, tmp.x)
                orientation.put(1, tmp.y)
                orientation.put(2, tmp.z)
                node.getUpVector(tmp)
                orientation.put(3, tmp.x)
                orientation.put(4, tmp.y)
                orientation.put(5, tmp.z)
                AL10.alListenerfv(AL10.AL_ORIENTATION, orientation)

                tmp.set(node.worldPosition)
                position.put(0, tmp.x)
                position.put(1, tmp.y)
                position.put(2, tmp.z)
                AL10.alListenerfv(AL10.AL_POSITION, position)
            } else {
                tmp.set(0f, 0f, -1f)
                orientation.put(0, tmp.x)
                orientation.put(1, tmp.y)
                orientation.put(2, tmp.z)
                tmp.set(0f, 1f, 0f)
                orientation.put(3, tmp.x)
                orientation.put(4, tmp.y)
                orientation.put(5, tmp.z)
                AL10.alListenerfv(AL10.AL_ORIENTATION, orientation)

                tmp.set(0f, 0f, 0f)
                position.put(0, tmp.x)
                position.put(1, tmp.y)
                position.put(2, tmp.z)
                AL10.alListenerfv(AL10.AL_POSITION, position)
            }
        }
    }
}
