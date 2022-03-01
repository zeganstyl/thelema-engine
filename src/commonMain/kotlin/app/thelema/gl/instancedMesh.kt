package app.thelema.gl

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.Blending
import app.thelema.g3d.IMaterial
import app.thelema.math.Frustum

interface IInstancedMesh: IRenderable {
    override val componentName: String
        get() = "InstancedMesh"

    var mesh: IMesh?

    var material: IMaterial?

    override var translucencyPriority: Int
        get() = material?.translucentPriority ?: 0
        set(_) {}

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    override fun visibleInFrustum(frustum: Frustum): Boolean = true

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMesh && mesh == null) mesh = component
    }
}
