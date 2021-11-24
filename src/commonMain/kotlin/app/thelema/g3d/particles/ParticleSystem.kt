package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.ecs.sibling
import app.thelema.g3d.IMaterial
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexBuffer
import app.thelema.gl.VertexBuffer
import app.thelema.math.IVec3
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.utils.LOG
import app.thelema.utils.iterate

class ParticleSystem: IParticleSystem {
    override val componentName: String
        get() = "ParticleSystem"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (mesh == null) mesh = value?.component()
            value?.component<IMaterial>()?.shader = shader
        }

    override var mesh: IMesh? = null
        set(value) {
            field = value
            if (value != null) {
                value.material = sibling()
                vertexBuffer.also { vertexBuffer ->
                    if (!value.vertexBuffers.contains(vertexBuffer)) {
                        value.addVertexBuffer(vertexBuffer)
                    }
                }
            }
        }

    private var updateParticleSystemRequested = false

    override var instancePositionName: String = "INSTANCE_POSITION"
    override var instanceLifeTimeName: String = "INSTANCE_LIFE_TIME"

    private val _emitters = ArrayList<IParticleEmitter>()
    override val emitters: List<IParticleEmitter>
        get() = _emitters

    override val particleNodes = ArrayList<IParticleNode>()

    override val shader: IShader = Shader()

    override val vertexBuffer: IVertexBuffer = VertexBuffer()

    /** If [vertexBuffer] capacity not enough, it's size will grow as bufferReserve * (visible particles count)  */
    override var bufferReserve: Float = 1.5f

    override val particlesData: MutableMap<String, MutableList<Any>> by lazy { HashMap() }

    override val particlesDataChannels = HashMap<String, IParticleDataChannel<Any?>>()

    private val _positions: MutableList<IVec3> = getOrCreateDataChannel<IVec3>(3, instancePositionName).data
    override val positions: List<IVec3>
        get() = _positions

    private var _lifeTimes: MutableList<Float> = ArrayList()
    override val lifeTimes: List<Float>
        get() = _lifeTimes

    private val _aliveParticles = ArrayList<Int>()

    override var lifeTimesAsAttribute = false
        set(value) {
            if (field != value) {
                field = value
                _lifeTimes = getOrCreateDataChannel<Float>(1, instanceLifeTimeName).data
                rebuildDataRequested = true
            } else {
                _lifeTimes = ArrayList()
            }
        }

    private var rebuildDataRequested = false

    override fun setParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] = time
    }

    override fun addParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] += time
    }

    override fun addParticleNode(node: IParticleNode) {
        val oldSize = vertexBuffer.vertexAttributes.size
        particleNodes.add(node)
        node.setupData(this)
        if (oldSize != vertexBuffer.vertexAttributes.size) rebuildDataRequested = true
    }

    override fun removeParticleNode(node: IParticleNode) {
        particleNodes.remove(node)
    }

    override fun addEmitter(emitter: IParticleEmitter) {
        _emitters.add(emitter)
    }

    override fun removeEmitter(emitter: IParticleEmitter) {
        _emitters.remove(emitter)
    }

    override fun requestUpdateParticleSystem() {
        updateParticleSystemRequested = true
    }

    override fun updateParticleSystem(delta: Float) {
        updateParticleSystemRequested = false

        mesh?.also { mesh ->
            var visibleParticles = 0

            _emitters.iterate {
                it.updateParticles(delta)
                visibleParticles += it.visibleParticles.size
            }

            particleNodes.iterate { node ->
                node.beginProcessParticleSystem(this, visibleParticles, delta)
                emitters.iterate { emitter ->
                    node.beginProcessEmitter(this, emitter, delta)
                    emitter.visibleParticles.iterate { particle ->
                        node.processParticle(this, particle, delta)
                    }
                }
            }

            if (vertexBuffer.verticesCount < visibleParticles || rebuildDataRequested) {
                rebuildDataRequested = false
                vertexBuffer.initVertexBuffer((visibleParticles * bufferReserve).toInt())
                vertexBuffer.setDivisor()
            }

            particlesDataChannels.values.forEach { channel ->
                val attribute = channel.attribute
                attribute.rewind()

                val channelData = channel.data
                _aliveParticles.iterate {
                    channel.setToAttribute(channelData[it])
                    attribute.nextVertex()
                }

                attribute.buffer.requestBufferUploading()
            }

            mesh.instancesCountToRender = visibleParticles
        }
    }

    val freeParticles = ArrayList<Int>()
    val freeParticlesSet = HashSet<Int>()

    override fun emitParticle(
        emitter: IParticleEmitter,
        maxLifeTime: Float,
        initialLifeTime: Float
    ): Int = if (freeParticles.isEmpty()) {
        _lifeTimes.size.also { particle ->
            if (!lifeTimesAsAttribute) _lifeTimes.add(initialLifeTime)
            particlesDataChannels.values.forEach { it.data.add(it.newDataElement()) }
            particleNodes.iterate { it.emitParticle(this, emitter, particle) }
        }
    } else {
        freeParticles.removeLast().also { i ->
            _lifeTimes[i] = initialLifeTime
            freeParticlesSet.remove(i)
        }
    }.also { _aliveParticles.add(it) }

    override fun shutdownParticle(particle: Int) {
        if (freeParticlesSet.add(particle)) {
            freeParticles.add(particle)
            _aliveParticles.remove(particle)
            particleNodes.iterate { it.shutdownParticle(this, particle) }
        } else {
            LOG.error("You can't shutdown particle $particle, it is already shutdown")
        }
    }

    override fun <T> getOrCreateDataChannel(dimension: Int, name: String): IParticleDataChannel<T> {
        val attribute = vertexBuffer.getOrCreateAttribute(dimension, name)

        var channel = particlesDataChannels[name] as IParticleDataChannel<T>?
        if (channel == null) {
            channel = when (dimension) {
                1 -> ParticleDataChannelBuilder.float
                2 -> ParticleDataChannelBuilder.vec2
                3 -> ParticleDataChannelBuilder.vec3
                4 -> ParticleDataChannelBuilder.vec4
                else -> throw IllegalStateException()
            }.build(attribute) as IParticleDataChannel<T>
            particlesDataChannels[name] = channel as IParticleDataChannel<Any?>
            rebuildDataRequested = true
        }

        return channel
    }
}