package app.thelema.g3d

import app.thelema.ecs.*
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object SceneInstanceMode {
    const val CopyWithoutScene = "CopyWithoutScene"
    const val FullCopy = "FullCopy"
    const val CopyWithoutRoot = "CopyWithoutRoot"

    var defaultMode: String = CopyWithoutScene
}

interface ISceneInstance: IEntityComponent {
    override val componentName: String
        get() = "SceneInstance"

    var sceneClassEntity: IEntity?

    var provider: ISceneProvider?

    var sceneInstance: IEntity?

    fun reloadInstance()
}

class SceneInstance: ComponentAdapter(), ISceneInstance {
    override var sceneClassEntity: IEntity? = null
        set(value) {
            if (field != value) {
                field?.removeEntityListener(entityListener)
                field = value
                if (enabled && value != null) {
                    when (mode) {
                        SceneInstanceMode.CopyWithoutScene -> {
                            value.addEntityListener(entityListener)
                            val entity = sceneEntity()
                            value.copyDeepInstance(to = entity, true)
                            sceneInstance = entity
                            entity.name = "Scene"
                            entity.serializeEntity = false
                        }
                        SceneInstanceMode.FullCopy -> {
                            value.addEntityListener(entityListener)
                            val entity = sceneEntity()
                            value.copyDeepInstance(to = entity, false)
                            sceneInstance = entity
                            entity.name = "Scene"
                            entity.serializeEntity = false
                        }
                    }
                }
            }
        }

    var mode: String = SceneInstanceMode.defaultMode

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

fun IEntity.setDeepInstance(other: IEntity) {
    val entitiesToRemove = ArrayList<IEntity>()

    forEachChildEntity { childEntity ->
        if (other.getEntityByName(childEntity.name) == null) {
            entitiesToRemove.add(childEntity)
        }
    }

    for (i in entitiesToRemove.indices) {
        removeEntity(entitiesToRemove[i])
    }

    for (i in other.children.indices) {
        val otherChild = other.children[i]
        entity(otherChild.name).setDeepInstance(otherChild)
    }


    name = other.name

    val componentsToRemove = ArrayList<IEntityComponent>()

    forEachComponent { component ->
        if (other.componentOrNull(component.componentName) == null) {
            componentsToRemove.add(component)
        }
    }

    for (i in componentsToRemove.indices) {
        removeComponent(componentsToRemove[i])
    }

    other.forEachComponent { otherComponent ->
        val component = component(otherComponent.componentName)
        component.setComponent(otherComponent)
        IEntityComponent.linkComponentListener(otherComponent, component)
    }
}

fun IEntity.setDeepInstanceWithoutRootScene(other: IEntity) {
    val entitiesToRemove = ArrayList<IEntity>()

    forEachChildEntity { childEntity ->
        if (other.getEntityByName(childEntity.name) == null) {
            entitiesToRemove.add(childEntity)
        }
    }

    for (i in entitiesToRemove.indices) {
        removeEntity(entitiesToRemove[i])
    }

    for (i in other.children.indices) {
        val otherChild = other.children[i]
        entity(otherChild.name).setDeepInstance(otherChild)
    }


    name = other.name

    val componentsToRemove = ArrayList<IEntityComponent>()

    forEachComponent { component ->
        if (other.componentOrNull(component.componentName) == null) {
            componentsToRemove.add(component)
        }
    }

    for (i in componentsToRemove.indices) {
        removeComponent(componentsToRemove[i])
    }

    other.forEachComponent { otherComponent ->
        if (otherComponent !is IScene) {
            val component = component(otherComponent.componentName)
            component.setComponent(otherComponent)
            IEntityComponent.linkComponentListener(otherComponent, component)
        }
    }
}

fun IEntity.copyDeepInstance(to: IEntity, withoutRootScene: Boolean, isRoot: Boolean = true): IEntity {
    forEachChildEntity {
        to.addEntity(it.copyDeepInstance(Entity(it.name), false, false))
    }

    if (withoutRootScene) {
        forEachComponent { if (it !is IScene) to.component(it.componentName) }
        if (isRoot) to.setDeepInstanceWithoutRootScene(this)
    } else {
        forEachComponent { to.component(it.componentName) }
        if (isRoot) to.setDeepInstance(this)
    }

    return to
}

fun IEntity.sceneInstance(block: ISceneInstance.() -> Unit) = component(block)
fun IEntity.sceneInstance() = component<ISceneInstance>()