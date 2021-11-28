package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.ui.Actor

interface IComponentScenePanel<C: IEntityComponent> {
    val componentSceneContent: Actor

    var component: C?
}