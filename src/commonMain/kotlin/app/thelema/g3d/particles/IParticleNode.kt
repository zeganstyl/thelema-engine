package app.thelema.g3d.particles

interface IParticleNode {
    fun setupData(particleSystem: IParticleSystem) {}

    fun beginProcessParticleSystem(system: IParticleSystem, visibleParticles: Int, delta: Float) {}

    fun beginProcessEmitter(system: IParticleSystem, emitter: IParticleEmitter, delta: Float) {}

    fun emitParticle(system: IParticleSystem, emitter: IParticleEmitter, particle: Int) {}

    fun shutdownParticle(system: IParticleSystem, particle: Int) {}

    fun processParticle(system: IParticleSystem, particle: Int, delta: Float) {}
}