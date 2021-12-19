package app.thelema.ecs

class TestComp: IEntityComponent {
    override val componentName: String
        get() = Name

    override var entityOrNull: IEntity? = null

    companion object {
        const val Name = "TestComp"
    }
}