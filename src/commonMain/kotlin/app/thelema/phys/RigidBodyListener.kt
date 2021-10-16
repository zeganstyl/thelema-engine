package app.thelema.phys

interface RigidBodyListener {
    fun collisionBegin(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {}
    fun collisionEnd(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {}
}
