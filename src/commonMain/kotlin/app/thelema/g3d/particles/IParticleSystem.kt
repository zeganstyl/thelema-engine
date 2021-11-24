package app.thelema.g3d.particles

import app.thelema.ecs.IEntityComponent
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexBuffer
import app.thelema.math.IVec2
import app.thelema.math.IVec3
import app.thelema.shader.IShader

interface IParticleSystem: IEntityComponent {
    override val componentName: String
        get() = "ParticleSystem"

    var mesh: IMesh?

    var instancePositionName: String
    var instanceLifeTimeName: String

    val emitters: List<IParticleEmitter>

    val particleNodes: List<IParticleNode>

    val shader: IShader

    val particlesData: MutableMap<String, MutableList<Any>>

    var bufferReserve: Float

    /** Elapsed lifetime of each particle. If particle has to be born, emitter must reset time to zero */
    val lifeTimes: List<Float>
    val positions: List<IVec3>

    /** Instanced buffer that must be filled with values of particles */
    val vertexBuffer: IVertexBuffer

    val particlesDataChannels: Map<String, IParticleDataChannel<Any?>>

    var lifeTimesAsAttribute: Boolean

    fun emitParticle(emitter: IParticleEmitter, maxLifeTime: Float, initialLifeTime: Float): Int

    fun shutdownParticle(particle: Int)

    fun setParticleLifeTime(particle: Int, time: Float)

    fun addParticleLifeTime(particle: Int, time: Float)

    fun <T: Any> getParticlesData(name: String): MutableList<T> = particlesData[name] as MutableList<T>

    fun addParticleNode(node: IParticleNode)

    fun addParticleNodes(vararg nodes: IParticleNode) {
        nodes.forEach { addParticleNode(it) }
    }

    fun removeParticleNode(node: IParticleNode)

    fun addEmitter(emitter: IParticleEmitter)

    fun removeEmitter(emitter: IParticleEmitter)

    fun requestUpdateParticleSystem()

    fun updateParticleSystem(delta: Float)

    fun <T> getOrCreateDataChannel(dimension: Int, name: String): IParticleDataChannel<T>

    fun <T> getDataChannel(id: String) = particlesDataChannels[id] as IParticleDataChannel<T>?
}