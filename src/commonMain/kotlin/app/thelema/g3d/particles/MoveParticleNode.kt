package app.thelema.g3d.particles

import app.thelema.math.Vec3

class MoveParticleNode: IParticleNode {
    val speed = Vec3(0f, 1f, 0f)

    private val tmp = Vec3()

    override fun beginProcessParticleSystem(system: IParticleSystem, visibleParticles: Int, delta: Float) {
        tmp.set(speed).scl(delta)
    }

    override fun processParticle(system: IParticleSystem, particle: Int, delta: Float) {
        system.positions[particle].add(tmp)
    }
}