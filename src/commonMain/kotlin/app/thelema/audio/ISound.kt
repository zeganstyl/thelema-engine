package app.thelema.audio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode

interface ISound: IEntityComponent {
    val sourceId: Int

    var soundLoader: ISoundLoader?

    val node: ITransformNode

    var pitch: Float

    var gain: Float

    var isLooped: Boolean

    var mustPlay: Boolean

    fun restart()

    fun playSound()

    fun pauseSound()

    fun stopSound()
}

fun IEntity.sound(block: ISound.() -> Unit) = component(block)
fun IEntity.sound() = component<ISound>()