package app.thelema.audio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode

class Sound3D: IEntityComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    override val componentName: String
        get() = "Sound3D"

    var node: ITransformNode = TransformNode()

    var pitch: Float = 0f

    var volume: Float = 1f

    var isLooped: Boolean = false


}