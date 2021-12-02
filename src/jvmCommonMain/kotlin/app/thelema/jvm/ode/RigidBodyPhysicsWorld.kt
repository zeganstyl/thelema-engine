/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.jvm.ode

import app.thelema.ecs.*
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.phys.*
import app.thelema.utils.Pool
import app.thelema.utils.iterate
import org.ode4j.math.DVector3C
import org.ode4j.ode.*
import kotlin.math.max

/** [ODE manual](http://ode.org/wiki/index.php?title=Manual)
 *
 * @author zeganstyl */
class RigidBodyPhysicsWorld: IRigidBodyPhysicsWorld {
    init {
        OdeHelper.initODE()
    }

    val simulationListener = object : SimulationListener {
        override fun startSimulation() {
            this@RigidBodyPhysicsWorld.startSimulation()
        }

        override fun stopSimulation() {
            this@RigidBodyPhysicsWorld.stopSimulation()
        }
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field?.component<SimulationNode>()?.removeSimulationListener(simulationListener)
            field = value
            value?.forEachComponent { addedSiblingComponent(it) }
            value?.forEachComponentInBranch { addedComponentToBranch(it) }
            value?.component<SimulationNode>()?.addSimulationListener(simulationListener)
        }

    var simulationRequest: Int = 0

    override var isSimulationRunning: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                simulationRequest = if (value) 1 else -1
            }
        }

    override var gravity: IVec3 = Vec3(0f, -3f, 0f)
        set(value) {
            field.set(value)
            world.setGravity(value.x.toDouble(), value.y.toDouble(), value.z.toDouble())
        }

    val bodies = ArrayList<RigidBody>()

    val bodyPairsPool = Pool { BodyContact(RigidBody(), RigidBody(), Vec3(), Vec3(), 0f) }

    val world = OdeHelper.createWorld().apply {
        // https://ode.org/ode-latest-userguide.html#sec_3_7_0
        erp = 0.2

        // https://ode.org/ode-latest-userguide.html#sec_3_8_2
        cfm = 0.0001

        quickStepNumIterations = 10
    }

    var space = OdeHelper.createHashSpace(null)
    val contactGroup = OdeHelper.createJointGroup()

    override var maxContacts = 40

    val newContacts = HashSet<BodyContact>()
    val currentContacts = HashSet<BodyContact>()
    val currentContactsMap = HashMap<Int, BodyContact>()
    val oldContacts = HashSet<BodyContact>()

    val listeners = ArrayList<IPhysicsWorldListener>()

    override var fixedDelta: Float = 0.02f

    var nearCallback = DGeom.DNearCallback { _, o1, o2 ->
        val g1 = o1.data as IShape
        val g2 = o2.data as IShape
        val b1 = o1.body
        val b2 = o2.body
        if (b1 != null && b2 != null && OdeHelper.areConnectedExcluding(b1, b2, DContactJoint::class.java)) return@DNearCallback
        val bm1 = (b1?.data ?: g1.body) as RigidBody?
        val bm2 = (b2?.data ?: g2.body) as RigidBody?
        val contacts = DContactBuffer(maxContacts) // up to MAX_CONTACTS contacts per box-box
        for (i in 0 until maxContacts) {
            val contact = contacts[i]
            contact.surface.mu = max(bm1?.friction ?: 1f, bm2?.friction ?: 1f).toDouble()

            contact.surface.mode = OdeConstants.dContactBounce or OdeConstants.dContactSoftCFM
            contact.surface.mu2 = 0.0
            contact.surface.bounce = 0.1
            contact.surface.bounce_vel = 0.1
            contact.surface.soft_cfm = 0.01
        }

        val numContacts = OdeHelper.collide(o1, o2, maxContacts, contacts.geomBuffer)
        if (numContacts != 0) {
            for (i in 0 until numContacts) {
                val contact = contacts[i]
                val c: DJoint = OdeHelper.createContactJoint(world, contactGroup, contact)

                val influence1 = bm1?.influenceOtherBodies ?: true
                val influence2 = bm2?.influenceOtherBodies ?: true

                // http://ode.org/wiki/index.php?title=Manual#How_do_I_make_.22one_way.22_collision_interaction
                when {
                    !influence1 && !influence2 -> {}
                    else -> {
                        if (influence1 && !influence2) {
                            c.attach(null, b2)
                        } else if (!influence1 && influence2) {
                            c.attach(b1, null)
                        } else {
                            c.attach(b1, b2)
                        }
                    }
                }

                // <Collect collisions> ================================================================================
                if (bm1 != null && bm2 != null) {
                    newContacts.add(bodyPairsPool.getOrCreate { BodyContact(bm1, bm2, contact) })
                }
                // </Collect collisions> ===============================================================================
            }
        }
    }

    override val sourceObject: Any
        get() = world

    init {
        gravity = gravity
    }

    override fun addedEntityToBranch(entity: IEntity) {
        entity.forEachChildEntity { child ->
            child.forEachComponentInBranch { addedComponentToBranch(it) }
        }
    }

    override fun removedEntityFromBranch(entity: IEntity) {
        entity.forEachChildEntity { child ->
            child.forEachComponentInBranch { removedComponentFromBranch(it) }
        }
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        if (component is RigidBody) {
            bodies.add(component)
            component.isSimulationRunning = isSimulationRunning
        }
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        addedComponentToBranch(component)
    }

    override fun addPhysicsWorldListener(listener: IPhysicsWorldListener) {
        listeners.add(listener)
    }

    override fun removePhysicsWorldListener(listener: IPhysicsWorldListener) {
        listeners.remove(listener)
    }

    override fun getContact(body1: IRigidBody, body2: IRigidBody): IBodyContact? =
        currentContactsMap[BodyContact.contactHash(body1, body2)]

    override fun step(delta: Float) {
        if (simulationRequest != 0) {
            isSimulationRunning = simulationRequest == 1
            simulationRequest = 0
            bodies.forEach { it.isSimulationRunning = isSimulationRunning }
        }

        if (isSimulationRunning) {
            space.collide(null, nearCallback)

            oldContacts.clear()
            oldContacts.addAll(currentContacts)

            currentContacts.clear()
            currentContacts.addAll(newContacts)

            currentContactsMap.clear()
            currentContacts.forEach { currentContactsMap[it.hashCode()] = it }

            newContacts.removeAll(oldContacts)
            oldContacts.removeAll(currentContacts)

            val iterations = max((delta / fixedDelta).toInt(), 1)
            for (i in 0 until iterations) {
                world.quickStep((if (fixedDelta > 0f) fixedDelta else delta).toDouble())
            }

            contactGroup.empty()

            oldContacts.forEach { contact ->
                bodyPairsPool.free(contact)
                for (j in listeners.indices) {
                    listeners[j].collisionEnd(contact)
                }
            }
            oldContacts.clear()

            newContacts.forEach { contact ->
                listeners.iterate { listener ->
                    listener.collisionBegin(contact)
                    contact.body1.collided(contact, contact.body1, contact.body2)
                    contact.body2.collided(contact, contact.body2, contact.body1)
                }
            }
            newContacts.clear()

            bodies.iterate { it.update() }
        }
    }

    override fun destroy() {
        bodies.iterate { it.destroy() }
        bodies.clear()
        contactGroup.destroy()
        space.destroy()
        world.destroy()
    }

    companion object {
        fun initOdeComponents() {
            ECS.descriptor({ RigidBodyPhysicsWorld() }) {
                setAliases(IRigidBodyPhysicsWorld::class)

                vec3(RigidBodyPhysicsWorld::gravity)
                float(RigidBodyPhysicsWorld::fixedDelta, 0.02f)
                int(RigidBodyPhysicsWorld::maxContacts, 40)
                bool(RigidBodyPhysicsWorld::isSimulationRunning)

                descriptor({ BoxShape() }) {
                    setAliases(IBoxShape::class)
                    float(BoxShape::mass, 1f)
                    float(BoxShape::xSize, 1f)
                    float(BoxShape::ySize, 1f)
                    float(BoxShape::zSize, 1f)
                }
                descriptor({ SphereShape() }) {
                    setAliases(ISphereShape::class)
                    float(SphereShape::radius, 1f)
                }
                descriptor({ TrimeshShape() }) {
                    setAliases(ITrimeshShape::class)
                    refAbs(TrimeshShape::mesh)
                }
                descriptor({ RigidBody() }) {
                    setAliases(IRigidBody::class)
                    float(RigidBody::maxAngularSpeed)
                    float(RigidBody::friction, 1f)
                    bool(RigidBody::isStatic, false)
                    bool(RigidBody::isGravityEnabled, true)
                    bool(RigidBody::isKinematic)
                    bool(RigidBody::influenceOtherBodies, true)
                }
            }
        }
    }
}

fun DVector3C.toVec3(out: IVec3 = Vec3()): IVec3 {
    return out.set(get0().toFloat(), get1().toFloat(), get2().toFloat())
}
