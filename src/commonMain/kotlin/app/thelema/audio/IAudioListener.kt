package app.thelema.audio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode

interface IAudioListener: IEntityComponent {
    override val componentName: String
        get() = "AudioListener"

    var node: ITransformNode?

    var gain: Float

    var useActiveCamera: Boolean

    var isEnabled: Boolean

    fun updateListener(delta: Float)
}

class AudioListenerStub: IAudioListener {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (node == null) node = value?.component() ?: TransformNode()
        }

    override var node: ITransformNode? = null

    override var gain: Float = 1f

    override var useActiveCamera: Boolean = true

    override var isEnabled: Boolean = true

    override fun updateListener(delta: Float) {}
}

fun IEntity.audioListener(block: IAudioListener.() -> Unit) = component(block)
fun IEntity.audioListener() = component<IAudioListener>()