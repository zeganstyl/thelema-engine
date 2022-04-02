package app.thelema.g3d.particles

import app.thelema.g3d.IUniformArgs
import app.thelema.gl.IInstancedMesh
import app.thelema.gl.IVertexAttribute
import app.thelema.gl.IVertexBuffer
import app.thelema.math.IVec4
import app.thelema.shader.IShader
import app.thelema.shader.useShader

/** Particles with same particle material */
interface IParticles {
    val particleMaterial: IParticleMaterial

    val emitters: List<IParticleEmitter>

    val particlesData: MutableMap<String, MutableList<Any>>

    val uniforms: IUniformArgs

    var bufferReserve: Float

    /** Stores vectors (x, y, z, w) for each particle,
     * where (x, y, z) - position and w - elapsed lifetime.
     * If particle has to be born, emitter must reset time to zero */
    val positionLife: List<IVec4>

    /** Instanced buffer that must be filled with values of particles */
    val vertexBuffer: IVertexBuffer

    val mesh: IInstancedMesh

    fun render(shader: IShader) {
        shader.useShader {
            shader.listener?.draw(shader)
            mesh.render()
        }
    }

    fun render(shaderChannel: String?)

    fun emitParticle(emitter: IParticleEmitter): Int

    fun shutdownParticle(particle: Int)

    fun setParticleLifeTime(particle: Int, time: Float)

    fun addParticleLifeTime(particle: Int, time: Float)

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getParticlesData(name: String): MutableList<T> = particlesData[name] as MutableList<T>

    fun addEmitter(emitter: IParticleEmitter)

    fun removeEmitter(emitter: IParticleEmitter)

    fun requestUpdateParticles()

    fun updateParticles(delta: Float)

    fun <T> getOrCreateDataChannel(attribute: IVertexAttribute): IParticleDataChannel<T>

    fun <T> getDataChannel(id: String): IParticleDataChannel<T>?

    fun destroyParticles()
}