package app.thelema.g3d

import app.thelema.ecs.IEntity

class SceneProvider: ISceneProvider {
    override val componentName: String
        get() = "SceneProvider"

    override var entityOrNull: IEntity? = null

    var proxy: ISceneProvider? = null

    override fun cancelProviding(instance: ISceneInstance) {
        proxy?.cancelProviding(instance)
    }

    override fun provideScene(instance: ISceneInstance) {
        proxy?.provideScene(instance)
    }
}