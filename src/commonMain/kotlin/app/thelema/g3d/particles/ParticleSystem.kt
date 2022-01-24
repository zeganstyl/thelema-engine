package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.UpdatableComponent
import app.thelema.g3d.Blending
import app.thelema.g3d.IScene
import app.thelema.gl.*
import app.thelema.math.Frustum
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.shader.IShader
import app.thelema.utils.iterate

class ParticleSystem: IParticleSystem {
    override val componentName: String
        get() = "ParticleSystem"

    override var entityOrNull: IEntity? = null

    override val particles = ArrayList<IParticles>()

    private val particlesMap: MutableMap<IParticleMaterial, IParticles> = HashMap()

    override var isVisible: Boolean = true

    override var alphaMode: String = Blending.OPAQUE

    override var translucencyPriority: Int = 1

    override val worldPosition: IVec3
        get() = MATH.Zero3

    override fun getOrCreateParticles(material: IParticleMaterial): IParticles {
        var particlesData = particlesMap[material]
        if (particlesData == null) {
            particlesData = Particles(material)
            particlesMap[material] = particlesData
            particles.add(particlesData)
        }
        return particlesData
    }

    override fun destroyParticles(material: IParticleMaterial) {
        particlesMap.remove(material)?.also {
            particles.remove(it)
            it.destroyParticles()
        }
    }

    override fun visibleInFrustum(frustum: Frustum): Boolean = true

    override fun render(scene: IScene?, shaderChannel: String?) {
        particles.iterate { it.mesh.render(scene, shaderChannel) }
    }

    override fun render(shader: IShader, scene: IScene?) {
        particles.iterate { it.mesh.render(shader, scene) }
    }

    override fun updateComponent(delta: Float) {
        particles.iterate { it.updateParticles(delta) }
    }
}