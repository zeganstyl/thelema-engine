package app.thelema.g3d.particles

interface ParticleEmissionListener {
    fun emitParticle(system: IParticleSystem, emitter: IParticleEmitter, particle: Int) {}

    fun shutdownParticle(system: IParticleSystem, particle: Int) {}
}