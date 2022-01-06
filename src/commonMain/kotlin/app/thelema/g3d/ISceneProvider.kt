package app.thelema.g3d

import app.thelema.ecs.IEntityComponent

interface ISceneProvider: IEntityComponent {
    val instances: List<ISceneInstance>

    fun cancelProviding(instance: ISceneInstance)

    fun provideScene(instance: ISceneInstance)
}