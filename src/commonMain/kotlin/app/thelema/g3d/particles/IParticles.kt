package app.thelema.g3d.particles

import app.thelema.gl.IMesh
import app.thelema.gl.IRenderable
import app.thelema.gl.IVertexBuffer
import app.thelema.math.IVec3

/** Particles with same particle material */
interface IParticles {
    val particleMaterial: IParticleMaterial

    val emitters: List<IParticleEmitter>

    val particlesData: MutableMap<String, MutableList<Any>>

    var bufferReserve: Float

    /** Elapsed lifetime of each particle. If particle has to be born, emitter must reset time to zero */
    val lifeTimes: List<Float>
    val positions: List<IVec3>

    /** Instanced buffer that must be filled with values of particles */
    val vertexBuffer: IVertexBuffer

    val mesh: IMesh

    fun emitParticle(emitter: IParticleEmitter, maxLifeTime: Float, initialLifeTime: Float): Int

    fun shutdownParticle(particle: Int)

    fun setParticleLifeTime(particle: Int, time: Float)

    fun addParticleLifeTime(particle: Int, time: Float)

    fun <T: Any> getParticlesData(name: String): MutableList<T> = particlesData[name] as MutableList<T>

    fun addEmitter(emitter: IParticleEmitter)

    fun removeEmitter(emitter: IParticleEmitter)

    fun requestUpdateParticles()

    fun updateParticles(delta: Float)

    fun <T> getOrCreateDataChannel(dimension: Int, name: String): IParticleDataChannel<T>

    fun <T> getDataChannel(id: String): IParticleDataChannel<T>?

    fun destroyParticles()
}