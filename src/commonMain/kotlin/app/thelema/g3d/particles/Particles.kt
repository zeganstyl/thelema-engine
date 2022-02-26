package app.thelema.g3d.particles

import app.thelema.gl.*
import app.thelema.math.IVec3
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/** See [IParticles] */
class Particles(override val particleMaterial: IParticleMaterial): IParticles {
    private var updateParticleSystemRequested = false

    private var lifeTimesAsAttribute = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    _lifeTimes = getOrCreateDataChannel<Float>(Particle.LIFE_TIME).data
                } else {
                    _lifeTimes = ArrayList()
                }
                rebuildDataRequested = true
            }
        }

    private val _aliveParticles = ArrayList<Int>()
    private val freeParticles = ArrayList<Int>()
    private val freeParticlesSet = HashSet<Int>()

    private val _emitters = ArrayList<IParticleEmitter>()
    override val emitters: List<IParticleEmitter>
        get() = _emitters

    private var _lifeTimes: MutableList<Float> = ArrayList()
    override val lifeTimes: List<Float>
        get() = _lifeTimes

    // TODO add max life times array

    override val vertexBuffer: IVertexBuffer = VertexBuffer()

    override val mesh: IMesh = Mesh().apply {
        addVertexBuffer(vertexBuffer)
        verticesCount = -1
    }

    private var rebuildDataRequested = false

    /** If [vertexBuffer] capacity not enough, it's size will grow as bufferReserve * (visible particles count)  */
    override var bufferReserve: Float = 1.5f

    override val particlesData: MutableMap<String, MutableList<Any>> by lazy { HashMap() }

    val particlesDataChannels = ArrayList<IParticleDataChannel<Any?>>()
    val particlesDataChannelsMap = HashMap<String, IParticleDataChannel<Any?>>()

    private val _positions: MutableList<IVec3> = getOrCreateDataChannel<IVec3>(Vertex.INSTANCE_POSITION).data
    override val positions: List<IVec3>
        get() = _positions

    override fun setParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] = time
    }

    override fun addParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] += time
    }

    override fun addEmitter(emitter: IParticleEmitter) {
        _emitters.add(emitter)
    }

    override fun removeEmitter(emitter: IParticleEmitter) {
        _emitters.remove(emitter)
    }

    override fun requestUpdateParticles() {
        updateParticleSystemRequested = true
    }

    override fun updateParticles(delta: Float) {
        if (updateParticleSystemRequested) {
            updateParticleSystemRequested = false

            mesh.inheritedMesh = particleMaterial.mesh
            mesh.material = particleMaterial.meshMaterial

            lifeTimesAsAttribute = particleMaterial.lifeTimesAsAttribute

            var visibleParticles = 0

            emitters.iterate { visibleParticles += it.visibleParticles.size }

            particleMaterial.particleEffects.iterate { node ->
                node.beginProcessParticles(this, visibleParticles, delta)
            }

            particleMaterial.processingEffects.iterate { listener ->
                emitters.iterate { emitter ->
                    listener.beginProcessEmitter(this, emitter, delta)
                    emitter.visibleParticles.iterate { particle ->
                        listener.processParticle(this, particle, delta)
                    }
                }
            }

            if (vertexBuffer.verticesCount < visibleParticles || rebuildDataRequested) {
                rebuildDataRequested = false
                vertexBuffer.initVertexBuffer((visibleParticles * bufferReserve).toInt())
            }

            particlesDataChannels.iterate { channel ->
                val accessor = channel.accessor
                accessor.rewind()

                val channelData = channel.data

                emitters.iterate { emitter ->
                    emitter.visibleParticles.iterate { particle ->
                        channel.setToAttribute(channelData[particle])
                        accessor.nextVertex()
                    }
                }

                accessor.buffer.requestBufferUploading()
            }

            vertexBuffer.gpuUploadRequested = visibleParticles > 0
            mesh.instancesCountToRender = visibleParticles
        }
    }

    override fun emitParticle(
        emitter: IParticleEmitter,
        maxLifeTime: Float,
        initialLifeTime: Float
    ): Int = if (freeParticles.isEmpty()) {
        _lifeTimes.size.also {
            if (!lifeTimesAsAttribute) _lifeTimes.add(initialLifeTime)
            particlesDataChannels.iterate { it.data.add(it.newDataElement()) }
        }
    } else {
        freeParticles.removeLast().also { i ->
            _lifeTimes[i] = initialLifeTime
            freeParticlesSet.remove(i)
        }
    }.also { particle ->
        _aliveParticles.add(particle)
        particleMaterial.emissionEffects.iterate { it.emitParticle(this, emitter, particle) }
    }

    override fun shutdownParticle(particle: Int) {
        if (freeParticlesSet.add(particle)) {
            freeParticles.add(particle)
            _aliveParticles.remove(particle)
            particleMaterial.emissionEffects.iterate { it.shutdownParticle(this, particle) }
        } else {
            LOG.error("You can't shutdown particle $particle, it is already shutdown")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOrCreateDataChannel(attribute: IVertexAttribute): IParticleDataChannel<T> {
        val accessor = vertexBuffer.getOrAddAttribute(attribute)

        var channel = particlesDataChannelsMap[attribute.name] as IParticleDataChannel<T>?
        if (channel == null) {
            channel = when (attribute.size) {
                1 -> ParticleDataChannelBuilder.float
                2 -> ParticleDataChannelBuilder.vec2
                3 -> ParticleDataChannelBuilder.vec3
                4 -> ParticleDataChannelBuilder.vec4
                else -> throw IllegalStateException()
            }.build(accessor) as IParticleDataChannel<T>
            particlesDataChannelsMap[attribute.name] = channel as IParticleDataChannel<Any?>
            particlesDataChannels.add(channel)
            rebuildDataRequested = true
            _aliveParticles.iterate { channel.data.add(channel.newDataElement()) }
            freeParticles.iterate { channel.data.add(channel.newDataElement()) }
        }

        return channel
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getDataChannel(id: String): IParticleDataChannel<T>? =
        particlesDataChannelsMap[id] as IParticleDataChannel<T>?

    override fun destroyParticles() {
        mesh.destroy()
    }
}