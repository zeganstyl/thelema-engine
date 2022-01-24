package app.thelema.g3d.particles

interface IParticleEffect {
    fun beginProcessParticles(particles: IParticles, visibleParticles: Int, delta: Float) {}
}