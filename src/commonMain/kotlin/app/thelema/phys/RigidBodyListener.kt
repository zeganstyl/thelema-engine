package app.thelema.phys

interface RigidBodyListener {
    fun contactBegin(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {}
    fun contactUpdated(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {}
    fun contactEnd(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {}
}
