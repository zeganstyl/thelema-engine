package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.UpdatableComponent
import app.thelema.ecs.component
import app.thelema.gl.IRenderable

/** Root object. Contains particles of all types on the scene.
 * System provides access to particles for emitters */
interface IParticleSystem: IEntityComponent, UpdatableComponent, IRenderable {
    override val componentName: String
        get() = "ParticleSystem"

    val particles: List<IParticles>

    fun getOrCreateParticles(material: IParticleMaterial): IParticles

    fun destroyParticles(material: IParticleMaterial)
}

fun IEntity.particleSystem(block: IParticleSystem.() -> Unit) = component(block)
fun IEntity.particleSystem() = component<IParticleSystem>()