package app.thelema.g3d

import app.thelema.ecs.*

class SceneInstance: ComponentAdapter(), ISceneInstance {
    override var sceneClassEntity: IEntity? = null
        set(value) {
            if (field != value) {
                value?.removeEntityListener(entityListener)
                field = value
                if (enabled && value != null) {
                    value.addEntityListener(entityListener)
                    val entity = entity.entity("Scene")
                    value.copyDeep(to = entity)
                    sceneInstance = entity
                    entity.name = "Scene"
                    entity.serializeEntity = false
                }
            }
        }

    var provider: ISceneProvider? = null
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
        override fun addedComponentToBranch(component: IEntityComponent) {
            if (enabled) {
                sceneClassEntity?.also {
                    val path = it.getRelativePathTo(component.entity)
                    entity.entity("Scene").makePath(path).component(component.componentName).setComponent(component)
                }
            }
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (enabled) {
                sceneClassEntity?.also { root ->
                    val path = root.getRelativePathTo(component.entity)
                    entity.entity("Scene").getEntityByPath(path)?.removeComponent(component.componentName)
                }
            }
        }
    }

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