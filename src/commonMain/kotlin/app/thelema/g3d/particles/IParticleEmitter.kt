package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.math.IVec3

interface IParticleEmitter: IEntityComponent {
    override val componentName: String
        get() = "ParticleEmitter"

    var particles: IParticles?

    var particleMaterial: IParticleMaterial?

    var maxParticles: Int

    /** Visible particles (emitted or already alive) */
    val visibleParticles: List<Int>

    var particleEmissionSpeed: Float

    var isPlaying: Boolean

    fun emitParticle(x: Float, y: Float, z: Float)

    fun emitParticle(position: IVec3) = emitParticle(position.x, position.y, position.z)

    fun updateParticles(delta: Float)
}

fun IEntity.particleEmitter(block: IParticleEmitter.() -> Unit) = component(block)
fun IEntity.particleEmitter() = component<IParticleEmitter>()