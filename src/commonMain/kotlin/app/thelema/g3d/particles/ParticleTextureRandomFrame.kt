package app.thelema.g3d.particles

import app.thelema.math.IVec2
import kotlin.random.Random

class ParticleTextureRandomFrame: IParticleNode {
    /** Horizontal frames count */
    var framesU = 1
        private set(value) {
            field = value
            smokeSizeU = 1f / framesU
        }
    /** Vertical frames count */
    var framesV = 1
        private set(value) {
            field = value
            smokeSizeV = 1f / framesV
        }
    var smokeSizeU = 1f / framesU
        private set
    var smokeSizeV = 1f / framesV
        private set

    lateinit var uvs: MutableList<IVec2>

    private var counter = 0

    var instanceUvStartName: String = "INSTANCE_UV_START"

    fun setupFrames(u: Int, v: Int) {
        framesU = u
        framesV = v
    }

    override fun setupData(particleSystem: IParticleSystem) {
        uvs = particleSystem.getOrCreateDataChannel<IVec2>(2, instanceUvStartName).data
    }

    override fun emitParticle(system: IParticleSystem, emitter: IParticleEmitter, particle: Int) {
        if (counter >= uvs.size) counter = 0
        uvs[particle].set(
            smokeSizeU * Random.nextInt(framesU),
            smokeSizeV * Random.nextInt(framesV)
        )
        counter++
    }
}