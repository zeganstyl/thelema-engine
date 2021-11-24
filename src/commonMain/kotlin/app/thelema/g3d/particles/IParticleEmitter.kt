package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.math.IVec2
import app.thelema.math.IVec3

interface IParticleEmitter: IEntityComponent {
    override val componentName: String
        get() = "ParticleEmitter"

    var particleSystem: IParticleSystem?

    var maxParticles: Int

    /** @return visible particles (emitted or already alive) */
    val visibleParticles: List<Int>

    /** If max lifetime is zero, particle must never die. */
    var maxLifeTime: Float

    fun updateParticles(delta: Float)
}

fun IEntity.particleEmitter(block: IParticleEmitter.() -> Unit) = component(block)
fun IEntity.particleEmitter() = component<IParticleEmitter>()