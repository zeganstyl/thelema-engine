package app.thelema.g3d

import app.thelema.ecs.*
import app.thelema.g3d.light.DirectionalLight

class SceneInstance: ComponentAdapter(), ISceneInstance {
    override var sceneClassEntity: IEntity? = null
        set(value) {
            if (field != value) {
                value?.removeEntityListener(entityListener)
                field = value
                if (enabled && value != null) {
                    value.addEntityListener(entityListener)
                    val entity = sceneEntity()
                    value.copyDeep(to = entity)
                    sceneInstance = entity
                    entity.name = "Scene"
                    entity.serializeEntity = false
                }
            }
        }

    override var provider: ISceneProvider? = null
        set(value) {
            field?.cancelProviding(this)
            field = value
            value?.provideScene(this)
        }

    override var sceneInstance: IEntity? = null

    var enabled = true
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    sceneClassEntity?.addEntityListener(entityListener)
                } else {
                    sceneClassEntity?.removeEntityListener(entityListener)
                }
            }
        }

    override var entityOrNull: IEntity?
        get() = super.entityOrNull
        set(value) {
            super.entityOrNull = value
            entityOrNull?.component<ITransformNode>()
        }

    private val entityListener = object : EntityListener {
        override fun addedEntityToBranch(entity: IEntity) {
            if (enabled) {
                sceneClassEntity?.also {
                    sceneEntity().makePath(it.getRelativePathTo(entity))
                }
            }
        }

        override fun removedEntityFromBranch(entity: IEntity) {
            if (enabled) {
                sceneClassEntity?.also {
                    sceneEntity().getEntityByPath(it.getRelativePathTo(entity))?.removeEntity()
                }
            }
        }

        override fun addedComponentToBranch(component: IEntityComponent) {
            if (enabled) {
                sceneClassEntity?.also {
                    val path = it.getRelativePathTo(component.entity)
                    val instanceComponent = sceneEntity().makePath(path).component(component.componentName)
                    instanceComponent.setComponent(component)
                    IEntityComponent.linkComponentListener(component, instanceComponent)
                }
            }
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (enabled) {
                sceneClassEntity?.also { root ->
                    val path = root.getRelativePathTo(component.entity)

                    val instanceComponent = sceneEntity().getEntityByPath(path)?.componentOrNull(component.componentName)
                    if (instanceComponent != null) {
                        IEntityComponent.linkComponentListener(component, instanceComponent)
                    }
                }
            }
        }
    }

    private fun sceneEntity() = entity.entity("Scene")

    override fun reloadInstance() {
        sceneInstance?.also { instance ->
            sceneClassEntity?.also { instance.setDeep(it) }
        }
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        super<ComponentAdapter>.addedComponentToBranch(component)
        if (component != this && component is SceneInstance) {
            component.enabled = false
        }
    }
}

fun IEntity.sceneInstance(block: ISceneInstance.() -> Unit) = component(block)
fun IEntity.sceneInstance() = component<ISceneInstance>()