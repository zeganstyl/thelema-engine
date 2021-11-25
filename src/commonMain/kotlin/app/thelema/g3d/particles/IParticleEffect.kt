package app.thelema.g3d.particles

interface IParticleEffect {
    fun setupParticleData(particleSystem: IParticleSystem) {}

    fun beginProcessParticleSystem(system: IParticleSystem, visibleParticles: Int, delta: Float) {}
}