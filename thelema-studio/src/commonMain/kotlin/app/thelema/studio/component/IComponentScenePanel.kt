package app.thelema.studio.component

import app.thelema.ecs.IEntityComponent
import app.thelema.ui.Actor

interface IComponentScenePanel<C: IEntityComponent> {
    val componentSceneContent: Actor

    var component: C?
}