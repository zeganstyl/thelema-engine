package app.thelema.ecs

/** Component that must be updated on main update loop */
interface UpdatableComponent {
    val updatePriority: Float
        get() = 0f

    val preUpdate: Boolean
        get() = false

    fun updateComponent(delta: Float)
}

interface RebuildListener {
    fun requestRebuild(component: ComponentCanBeRebuild)

    fun cancelRebuild(component: ComponentCanBeRebuild)
}

/** Component that must be build on main update loop */
interface ComponentCanBeRebuild {
    var rebuildListener: RebuildListener?

    var rebuildComponentRequested: Boolean

    fun requestRebuild() {
        if (!rebuildComponentRequested) {
            rebuildComponentRequested = true
            rebuildListener?.requestRebuild(this)
        }
    }

    fun cancelRebuild() {
        rebuildComponentRequested = false
        rebuildListener?.cancelRebuild(this)
    }

    fun rebuildComponent()
}