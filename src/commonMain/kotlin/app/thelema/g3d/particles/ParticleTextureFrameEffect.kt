package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.math.IVec2

class ParticleTextureFrameEffect: IParticleEffect, ParticleEmissionListener, IEntityComponent {
    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "ParticleTextureFrameEffect"

    /** Horizontal frames count */
    var framesU = 1
        set(value) {
            field = value
            sizeU = 1f / framesU
        }

    /** Vertical frames count */
    var framesV = 1
        set(value) {
            field = value
            sizeV = 1f / framesV
        }

    /** Horizontal frame size for shader */
    var sizeU = 1f / framesU
        private set

    /** Vertical frame size for shader */
    var sizeV = 1f / framesV
        private set

    lateinit var uvs: MutableList<IVec2>

    private var counter = 0

    var instanceUvStartName: String = "INSTANCE_UV_START"

    fun setupFrames(u: Int, v: Int) {
        framesU = u
        framesV = v
    }

    override fun setupParticleData(particleSystem: IParticleSystem) {
        uvs = particleSystem.getOrCreateDataChannel<IVec2>(2, instanceUvStartName).data
    }

    override fun emitParticle(system: IParticleSystem, emitter: IParticleEmitter, particle: Int) {
        if (counter >= uvs.size) counter = 0
        uvs[particle].set(
            sizeU * counter % framesU,
            sizeV * particle % framesV
        )
        counter++
    }
}