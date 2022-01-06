package app.thelema.g3d

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent

interface ISceneInstance: IEntityComponent {
    override val componentName: String
        get() = "SceneInstance"

    var sceneClassEntity: IEntity?

    var sceneInstance: IEntity?

    fun reloadInstance()
}