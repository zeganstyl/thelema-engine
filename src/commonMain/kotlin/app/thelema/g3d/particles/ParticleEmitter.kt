package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.math.IVec3
import app.thelema.utils.iterate

class ParticleEmitter: IParticleEmitter {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            _node = value?.component() ?: TransformNode()
        }

    override var particleSystem: IParticleSystem? = null
        set(value) {
            field?.removeEmitter(this)
            field = value
            if (isPlaying) {
                value?.also {
                    if (!it.emitters.contains(this)) it.addEmitter(this)
                }
            }
        }

    var placeParticle: (index: Int, out: IVec3) -> Unit = { _, out ->
        out.set(node.worldPosition)
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

    override var maxParticleLifeTime = 1f

    var particlesEmissionSpeedInv = if (particleEmissionSpeed != 0f) 1 / particleEmissionSpeed else 0f
        private set

    private var emissionTime = 0f

    private val toShutdown = ArrayList<Int>()
    private val _visibleParticles = ArrayList<Int>()
    override val visibleParticles: List<Int>
        get() = _visibleParticles

    override var isPlaying = true
        set(value) {
            field = value
            particleSystem?.also {
                if (value) {
                    if (!it.emitters.contains(this)) it.addEmitter(this)
                } else {
                    it.removeEmitter(this)
                }
            }
        }

    override fun emitParticle(x: Float, y: Float, z: Float) {
        particleSystem?.also { particleSystem ->
            val particle = particleSystem.emitParticle(this, this.maxParticleLifeTime, 0f)
            _visibleParticles.add(particle)
            particleSystem.positions[particle].set(x, y, z)
        }
    }

    override fun updateParticles(delta: Float) {
        particleSystem?.also { particleSystem ->

            if (emissionTime > 0f) {
                emissionTime -= delta
            } else if (emissionTime < 0f) {
                emissionTime = 0f
            }

            toShutdown.clear()
            for (i in _visibleParticles.indices) {
                val particle = _visibleParticles[i]
                val time = particleSystem.lifeTimes[particle]
                if (time <= this.maxParticleLifeTime) {
                    particleSystem.addParticleLifeTime(particle, delta)
                }
                if (time > this.maxParticleLifeTime) {
                    particleSystem.setParticleLifeTime(particle, this.maxParticleLifeTime)
                    toShutdown.add(i)
                }
            }

            var j = toShutdown.size
            while (j > 0) {
                j--
                particleSystem.shutdownParticle(_visibleParticles.removeAt(toShutdown[j]))
            }

            if (_visibleParticles.size < maxParticles && particleEmissionSpeed != 0f) {
                for (i in 0 until maxParticles) {
                    if (emissionTime <= delta) {
                        emissionTime += particlesEmissionSpeedInv
                        val particle = particleSystem.emitParticle(this, this.maxParticleLifeTime, if (emissionTime < delta) emissionTime else 0f)
                        _visibleParticles.add(particle)
                        placeParticle(particle, particleSystem.positions[particle])
                    }
                }
            }

            if (_visibleParticles.isNotEmpty()) particleSystem.requestUpdateParticleSystem()
        }
    }

    override fun destroy() {
        particleSystem?.also { particleSystem ->
            _visibleParticles.iterate {
                particleSystem.shutdownParticle(it)
            }
        }
        particleSystem = null
    }
}