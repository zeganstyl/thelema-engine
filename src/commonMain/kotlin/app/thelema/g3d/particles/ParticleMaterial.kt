package app.thelema.g3d.particles

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.siblingOrNull
import app.thelema.g3d.IMaterial
import app.thelema.g3d.Material
import app.thelema.gl.IMesh
import app.thelema.shader.IShader

class ParticleMaterial: IParticleMaterial {
    override val componentName: String
        get() = "ParticleMaterial"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            meshMaterial = value?.component() ?: Material()
            if (mesh == null) mesh = siblingOrNull()
            value?.component<IShader>()
            value?.forEachComponent { if (it is IParticleEffect) addParticleEffect(it) }
            value?.forEachChildEntity { child ->
                child.forEachComponent { if (it is IParticleEffect) addParticleEffect(it) }
            }
        }

    override var meshMaterial: IMaterial = Material()

    override val emissionEffects = ArrayList<ParticleEmissionEffect>()
    override val processingEffects = ArrayList<ParticleProcessingEffect>()
    override val particleEffects = ArrayList<IParticleEffect>()

    override var mesh: IMesh? = null

    override var instancePositionName: String = "INSTANCE_POSITION"
    override var instanceLifeTimeName: String = "INSTANCE_LIFE_TIME"

    override var lifeTimesAsAttribute = false

    override fun addParticleEffect(effect: IParticleEffect) {
        if (effect is ParticleProcessingEffect) processingEffects.add(effect)
        if (effect is ParticleEmissionEffect) emissionEffects.add(effect)
        particleEffects.add(effect)
    }

    override fun removeParticleEffect(effect: IParticleEffect) {
        if (effect is ParticleProcessingEffect) processingEffects.remove(effect)
        if (effect is ParticleEmissionEffect) emissionEffects.remove(effect)
        particleEffects.remove(effect)
    }

    override fun addedChildComponent(component: IEntityComponent) {
        if (component is IParticleEffect) addParticleEffect(component)
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IParticleEffect) addParticleEffect(component)
    }

    override fun removedChildComponent(component: IEntityComponent) {
        if (component is IParticleEffect) removeParticleEffect(component)
    }

    override fun removedSiblingComponent(component: IEntityComponent) {
        if (component is IParticleEffect) removeParticleEffect(component)
    }
}

interface IParticleMaterial: IEntityComponent {
    override val componentName: String
        get() = "ParticleMaterial"

    var instancePositionName: String
    var instanceLifeTimeName: String

    var lifeTimesAsAttribute: Boolean

    var meshMaterial: IMaterial

    val emissionEffects: List<ParticleEmissionEffect>
    val processingEffects: List<ParticleProcessingEffect>
    val particleEffects: List<IParticleEffect>

    var mesh: IMesh?

    fun addParticleEffect(effect: IParticleEffect)

    fun removeParticleEffect(effect: IParticleEffect)
}

fun IEntity.particleMaterial(block: IParticleMaterial.() -> Unit) = component(block)
fun IEntity.particleMaterial() = component<IParticleMaterial>()
