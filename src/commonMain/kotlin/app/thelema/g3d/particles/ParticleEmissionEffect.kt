package app.thelema.g3d.particles

interface ParticleEmissionEffect {
    fun emitParticle(particles: IParticles, emitter: IParticleEmitter, particle: Int) {}

    fun shutdownParticle(particles: IParticles, particle: Int) {}
}