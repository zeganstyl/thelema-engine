package app.thelema.g3d.particles

interface ParticleProcessingEffect {
    fun beginProcessEmitter(particles: IParticles, emitter: IParticleEmitter, delta: Float) {}

    fun processParticle(particles: IParticles, particle: Int, delta: Float) {}
}