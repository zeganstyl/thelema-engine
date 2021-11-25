package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.math.IVec3
import app.thelema.math.Vec3

class MoveParticleEffect: IParticleEffect, ParticleProcessingListener, IEntityComponent {
    var speed: IVec3 = Vec3(0f, 1f, 0f)
        set(value) { field.set(value) }

    private val tmp = Vec3()

    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "MoveParticleEffect"

    override fun beginProcessParticleSystem(system: IParticleSystem, visibleParticles: Int, delta: Float) {
        tmp.set(speed).scl(delta)
    }

    override fun processParticle(system: IParticleSystem, particle: Int, delta: Float) {
        system.positions[particle].add(tmp)
    }
}