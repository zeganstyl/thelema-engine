package app.thelema.g3d.particles

interface ParticleProcessingListener {
    fun beginProcessEmitter(system: IParticleSystem, emitter: IParticleEmitter, delta: Float) {}

    fun processParticle(system: IParticleSystem, particle: Int, delta: Float) {}
}