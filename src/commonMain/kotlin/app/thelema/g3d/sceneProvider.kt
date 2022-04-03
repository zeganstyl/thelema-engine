package app.thelema.g3d

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent

interface ISceneProvider: IEntityComponent {
    val sceneInstances: List<ISceneInstance>

    fun cancelProviding(instance: ISceneInstance)

    fun provideScene(instance: ISceneInstance)
}

class SceneProvider: ISceneProvider {
    override val componentName: String
        get() = "SceneProvider"

    override var entityOrNull: IEntity? = null

    var proxy: SceneProviderProxy? = null

    override val sceneInstances: List<ISceneInstance>
        get() = proxy?.sceneInstances ?: emptyList()

    override fun cancelProviding(instance: ISceneInstance) {
        proxy?.cancelProviding(instance)
    }

    override fun provideScene(instance: ISceneInstance) {
        proxy?.provideScene(instance)
    }
}

interface SceneProviderProxy {
    val sceneInstances: List<ISceneInstance>

    fun cancelProviding(instance: ISceneInstance)

    fun provideScene(instance: ISceneInstance)
}