package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.sibling
import app.thelema.g3d.IMaterial
import app.thelema.g3d.mesh.planeMesh
import app.thelema.gl.GL
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexBuffer
import app.thelema.gl.VertexBuffer
import app.thelema.img.texture2D
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.res.RES
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.node.*
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
            value?.forEachComponent { if (it is IParticleEffect) addParticleEffect(it) }
            value?.planeMesh {
                normal = MATH.Z
                setSize(1f)
            }
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

    private val _aliveParticles = ArrayList<Int>()
    private val freeParticles = ArrayList<Int>()
    private val freeParticlesSet = HashSet<Int>()

    private val _emitters = ArrayList<IParticleEmitter>()
    override val emitters: List<IParticleEmitter>
        get() = _emitters

    private var _lifeTimes: MutableList<Float> = ArrayList()
    override val lifeTimes: List<Float>
        get() = _lifeTimes

    override val particleEffects = ArrayList<IParticleEffect>()

    override val shader: IShader = Shader()

    override val vertexBuffer: IVertexBuffer = VertexBuffer()

    private var rebuildDataRequested = false

    private val processingListeners = ArrayList<ParticleProcessingListener>()
    private val emissionListeners = ArrayList<ParticleEmissionListener>()

    /** If [vertexBuffer] capacity not enough, it's size will grow as bufferReserve * (visible particles count)  */
    override var bufferReserve: Float = 1.5f

    override val particlesData: MutableMap<String, MutableList<Any>> by lazy { HashMap() }

    override val particlesDataChannels = HashMap<String, IParticleDataChannel<Any?>>()

    private val _positions: MutableList<IVec3> = getOrCreateDataChannel<IVec3>(3, instancePositionName).data
    override val positions: List<IVec3>
        get() = _positions

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

    override fun setParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] = time
    }

    override fun addParticleLifeTime(particle: Int, time: Float) {
        _lifeTimes[particle] += time
    }

    override fun addParticleEffect(effect: IParticleEffect) {
        if (effect is ParticleProcessingListener) processingListeners.add(effect)
        if (effect is ParticleEmissionListener) emissionListeners.add(effect)
        particleEffects.add(effect)
        effect.setupParticleData(this)
    }

    override fun removeParticleEffect(effect: IParticleEffect) {
        if (effect is ParticleProcessingListener) processingListeners.remove(effect)
        if (effect is ParticleEmissionListener) emissionListeners.remove(effect)
        particleEffects.remove(effect)
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
            val visibleParticles = _aliveParticles.size

            particleEffects.iterate { node ->
                node.beginProcessParticleSystem(this, visibleParticles, delta)
            }

            processingListeners.iterate { listener ->
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



    init {
        // FIXME

        GL.call {
            shader.apply {
                val vertex = addNode(VertexNode())

                val particleData = addNode(ParticleDataNode())
                particleData.maxLifeTime = 5f
                particleData.instanceLifeTimeName = instanceLifeTimeName

                val scaling = addNode(ParticleScaleNode().apply {
                    scaling = 5f
                    lifeTimePercent = particleData.lifeTimePercent
                    inputPosition = vertex.position
                })

                val camera = addNode(CameraDataNode(scaling.scaledPosition))
                camera.useInstancePosition = true
                camera.alwaysRotateObjectToCamera = true

                val uvNode = addNode(UVNode().apply {
                    uvName = "UV"
                })

                val textureFrame = addNode(ParticleUVFrameNode().apply {
                    inputUv = uvNode.uv
                    frameSizeU = 1f / 6f
                    frameSizeV = 1f / 5f
                })

                val texture = addNode(TextureNode(texture = RES.texture2D("Smoke30Frames.png")))
                texture.uv = textureFrame.resultUv

                val particleColor = addNode(ParticleColorNode().apply {
                    lifeTimePercent = particleData.lifeTimePercent
                    inputColor = texture.color
                    color1 = GLSLVec4Inline(1f, 1f, 1f, 0f)
                })

                addNode(OutputNode(camera.clipSpacePosition, particleColor.result))

                depthMask = false

                build()

                println(printCode())
            }
        }
    }


    override fun addedChildComponent(component: IEntityComponent) {
        if (component is IParticleEffect) addParticleEffect(component)
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IParticleEffect) addParticleEffect(component)
    }

    override fun removedChildComponent(component: IEntityComponent) {
        if (component is IParticleEffect) addParticleEffect(component)
    }

    override fun removedSiblingComponent(component: IEntityComponent) {
        if (component is IParticleEffect) removeParticleEffect(component)
    }

    override fun emitParticle(
        emitter: IParticleEmitter,
        maxLifeTime: Float,
        initialLifeTime: Float
    ): Int = if (freeParticles.isEmpty()) {
        _lifeTimes.size.also { particle ->
            if (!lifeTimesAsAttribute) _lifeTimes.add(initialLifeTime)
            particlesDataChannels.values.forEach { it.data.add(it.newDataElement()) }
            emissionListeners.iterate { it.emitParticle(this, emitter, particle) }
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
            emissionListeners.iterate { it.shutdownParticle(this, particle) }
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
            _aliveParticles.iterate { channel.data.add(channel.newDataElement()) }
            freeParticles.iterate { channel.data.add(channel.newDataElement()) }
        }

        return channel
    }
}