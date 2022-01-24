package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.math.IVec3
import app.thelema.math.Vec3

class MoveParticleEffect: IParticleEffect, ParticleProcessingEffect, IEntityComponent {
    var speed: IVec3 = Vec3(0f, 1f, 0f)
        set(value) { field.set(value) }

    private val tmp = Vec3()

    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "MoveParticleEffect"

    override fun beginProcessParticles(particles: IParticles, visibleParticles: Int, delta: Float) {
        tmp.set(speed).scl(delta)
    }

    override fun processParticle(particles: IParticles, particle: Int, delta: Float) {
        particles.positions[particle].add(tmp)
    }
}