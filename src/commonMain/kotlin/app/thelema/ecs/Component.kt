package app.thelema.ecs

abstract class Component: IEntityComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            val oldEntity = field
            field = value
            value?.also { onAttachedToEntity(it, oldEntity) }
        }

    open fun onAttachedToEntity(entity: IEntity, old: IEntity?) {}
}