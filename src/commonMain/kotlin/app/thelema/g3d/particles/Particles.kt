package app.thelema.g3d.particles

import app.thelema.g3d.IUniformArgs
import app.thelema.g3d.UniformArgs
import app.thelema.gl.*
import app.thelema.math.IVec4
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/** See [IParticles] */
class Particles(override val particleMaterial: IParticleMaterial): IParticles {
    private var updateParticlesRequested = false

    private val _aliveParticles = ArrayList<Int>()
    private val freeParticles = ArrayList<Int>()
    private val freeParticlesSet = HashSet<Int>()

    private val _emitters = ArrayList<IParticleEmitter>()
    override val emitters: List<IParticleEmitter>
        get() = _emitters

    override val vertexBuffer: IVertexBuffer = VertexBuffer()

    override val mesh: IInstancedMesh = InstancedMesh().apply {
        addVertexBuffer(vertexBuffer)
    }

    private var rebuildDataRequested = false

    /** If [vertexBuffer] capacity not enough, it's size will grow as bufferReserve * (visible particles count)  */
    override var bufferReserve: Float = 1.5f

    override val particlesData: MutableMap<String, MutableList<Any>> by lazy { HashMap() }

    val particlesDataChannels = ArrayList<IParticleDataChannel<Any?>>()
    val particlesDataChannelsMap = HashMap<String, IParticleDataChannel<Any?>>()

    private val _positionLife: MutableList<IVec4> = getOrCreateDataChannel<IVec4>(Particle.POSITION_LIFE).data
    override val positionLife: List<IVec4>
        get() = _positionLife

    override val uniforms: IUniformArgs = UniformArgs()

    val meshUbo = MeshUniformBuffer()

    val particlesUbo = ParticleUniformBuffer()

    override fun setParticleLifeTime(particle: Int, time: Float) {
        _positionLife[particle].w = time
    }

    override fun addParticleLifeTime(particle: Int, time: Float) {
        _positionLife[particle].w += time
    }

    override fun addEmitter(emitter: IParticleEmitter) {
        _emitters.add(emitter)
    }

    override fun removeEmitter(emitter: IParticleEmitter) {
        _emitters.remove(emitter)
    }

    override fun requestUpdateParticles() {
        updateParticlesRequested = true
    }

    override fun updateParticles(delta: Float) {
        if (updateParticlesRequested) {
            updateParticlesRequested = false

            mesh.material = particleMaterial.material
            mesh.mesh = particleMaterial.mesh

            meshUbo.bonesNum(0)
            particlesUbo.maxLifeTime(particleMaterial.maxLifeTime)

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
            mesh.instancesCount = visibleParticles
        }
    }

    override fun render(shaderChannel: String?) {
        particleMaterial.material.getChannel(shaderChannel)?.also {
            it.bindUniformBuffer(meshUbo)
            it.bindUniformBuffer(particlesUbo)
            render(it)
        }
    }

    override fun emitParticle(emitter: IParticleEmitter): Int = if (freeParticles.isEmpty()) {
        _positionLife.size.also {
            particlesDataChannels.iterate { it.data.add(it.newDataElement()) }
        }
    } else {
        freeParticles.removeLast().also { freeParticlesSet.remove(it) }
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
        val accessor = vertexBuffer.getOrAddAccessor(attribute)

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