package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.UpdatableComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.math.IVec4
import app.thelema.utils.iterate

class ParticleEmitter: IParticleEmitter, UpdatableComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            _node = value?.component() ?: TransformNode()
            setupParticles()
        }

    override var particles: IParticles? = null
        set(value) {
            field?.removeEmitter(this)
            field = value
            value?.addEmitter(this)
        }

    override var particleMaterial: IParticleMaterial? = null
        set(value) {
            field = value
            setupParticles()
        }

    var placeParticle: (index: Int, out: IVec4) -> Unit = { _, out ->
        out.setVec3(node.worldPosition)
    }

    private var _node: ITransformNode = TransformNode()
    val node: ITransformNode
        get() = _node

    override var maxParticles = 100

    override var particleEmissionSpeed = 1f
        set(value) {
            field = value
            particlesEmissionSpeedInv = if (particleEmissionSpeed != 0f) 1 / particleEmissionSpeed else 0f
        }

    var particlesEmissionSpeedInv = if (particleEmissionSpeed != 0f) 1 / particleEmissionSpeed else 0f
        private set

    private var emissionTime = 0f

    private val toShutdown = ArrayList<Int>()
    private val _visibleParticles = ArrayList<Int>()
    override val visibleParticles: List<Int>
        get() = _visibleParticles

    override var isPlaying: Boolean = true

    private fun setupParticles() {
        particleMaterial?.also {
            particles = entityOrNull?.getRootEntity()?.particleSystem()?.getOrCreateParticles(it)
        }
    }

    override fun emitParticle(x: Float, y: Float, z: Float) {
        particles?.also { particleSystem ->
            val particle = particleSystem.emitParticle(this)
            _visibleParticles.add(particle)
            particleSystem.positionLife[particle].set(x, y, z, 0f)
        }
    }

    override fun updateComponent(delta: Float) {
        updateParticles(delta)
    }

    override fun updateParticles(delta: Float) {
        if (isPlaying) {
            particles?.also { particles ->

                if (emissionTime > 0f) {
                    emissionTime -= delta
                } else if (emissionTime < 0f) {
                    emissionTime = 0f
                }

                toShutdown.clear()
                val maxLife = particles.particleMaterial.maxLifeTime
                for (i in _visibleParticles.indices) {
                    val particle = _visibleParticles[i]
                    val time = particles.positionLife[particle].w
                    if (time <= maxLife) {
                        particles.addParticleLifeTime(particle, delta)
                    }
                    if (time > maxLife) {
                        particles.setParticleLifeTime(particle, maxLife)
                        toShutdown.add(i)
                    }
                }

                var j = toShutdown.size
                while (j > 0) {
                    j--
                    particles.shutdownParticle(_visibleParticles.removeAt(toShutdown[j]))
                }

                if (_visibleParticles.size < maxParticles && particleEmissionSpeed != 0f) {
                    for (i in 0 until maxParticles) {
                        if (emissionTime <= delta) {
                            emissionTime += particlesEmissionSpeedInv
                            val particle = particles.emitParticle(this)
                            particles.positionLife[particle].w = if (emissionTime < delta) emissionTime else 0f
                            _visibleParticles.add(particle)
                            placeParticle(particle, particles.positionLife[particle])
                        }
                    }
                }

                if (_visibleParticles.isNotEmpty() || toShutdown.isNotEmpty()) particles.requestUpdateParticles()
            }
        }
    }

    override fun destroy() {
        particles?.also { particleSystem ->
            _visibleParticles.iterate {
                particleSystem.shutdownParticle(it)
            }
        }
        particles = null
    }
}