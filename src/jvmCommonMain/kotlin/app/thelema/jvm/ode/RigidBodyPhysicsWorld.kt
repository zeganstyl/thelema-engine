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
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.phys.*
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

    override var gravity: IVec3 = Vec3(0f, -9.8f, 0f)
        set(value) {
            field.set(value)
            world.setGravity(value.x.toDouble(), value.y.toDouble(), value.z.toDouble())
        }

    val bodies = ArrayList<RigidBody>()

    val world = OdeHelper.createWorld().apply {
        // https://ode.org/ode-latest-userguide.html#sec_3_7_0
        erp = 0.2

        // https://ode.org/ode-latest-userguide.html#sec_3_8_2
        cfm = 0.0001

        quickStepNumIterations = iterations
    }

    var space = OdeHelper.createHashSpace(null)
    val contactGroup = OdeHelper.createJointGroup()

    override var maxContacts = 40

    val newContacts = ArrayList<BodyContact>()
    val currentContacts = HashSet<BodyContact>()
    val updatedContacts = ArrayList<BodyContact>()
    val endContacts = HashSet<BodyContact>()
    val previousContacts = HashSet<BodyContact>()

    val listeners = ArrayList<IPhysicsWorldListener>()

    override var fixedDelta: Float = 0.02f

    override var iterations: Int = 10
        set(value) {
            field = value
            world?.quickStepNumIterations = value
        }

    override var useQuickStep: Boolean = true

    var nearCallback = DGeom.DNearCallback { _, o1, o2 ->
        val g1 = o1.data as IPhysicalShape
        val g2 = o2.data as IPhysicalShape
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
                    val bc = BodyContact(bm1, bm2, contact)
                    if (currentContacts.add(bc)) {
                        if (endContacts.remove(bc)) {
                            updatedContacts.add(bc)
                        } else {
                            newContacts.add(bc)
                        }
                    }
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

    override fun step(delta: Float) {
        if (simulationRequest != 0) {
            isSimulationRunning = simulationRequest == 1
            simulationRequest = 0
            bodies.iterate { it.isSimulationRunning = isSimulationRunning }
        }

        if (isSimulationRunning) {
            currentContacts.clear()
            newContacts.clear()
            updatedContacts.clear()
            endContacts.clear()
            endContacts.addAll(previousContacts)
            space.collide(null, nearCallback)

            previousContacts.clear()
            previousContacts.addAll(currentContacts)

            if (useQuickStep) {
                world.quickStep(fixedDelta.toDouble())
            } else {
                for (i in 0 until iterations) {
                    world.step(fixedDelta.toDouble())
                }
            }

            contactGroup.empty()

            endContacts.removeAll(currentContacts)
            if (endContacts.isNotEmpty()) {
                endContacts.forEach { contact ->
                    listeners.iterate { it.contactEnd(contact) }
                    val b1 = contact.body1
                    val b2 = contact.body2
                    b1.contactEnd(contact, b1, b2)
                    b2.contactEnd(contact, b2, b1)
                }
                endContacts.clear()
            }

            updatedContacts.iterate { contact ->
                listeners.iterate { it.contactUpdated(contact) }
                val b1 = contact.body1
                val b2 = contact.body2
                b1.contactUpdated(contact, b1, b2)
                b2.contactUpdated(contact, b2, b1)
            }
            updatedContacts.clear()

            newContacts.iterate { contact ->
                listeners.iterate { it.contactBegin(contact) }
                val b1 = contact.body1
                val b2 = contact.body2
                b1.contactBegin(contact, b1, b2)
                b2.contactBegin(contact, b2, b1)
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
        private fun <T: IPhysicalShape> ComponentDescriptor<T>.setupShape() {
            val desc = this as ComponentDescriptor<IPhysicalShape>
            desc.float(IPhysicalShape::mass)
            desc.vec3(IPhysicalShape::positionOffset)
            desc.quaternion(IPhysicalShape::rotationOffset)
        }

        fun initOdeComponents() {
            ECS.descriptorI<IRigidBodyPhysicsWorld>(::RigidBodyPhysicsWorld) {
                vec3(IRigidBodyPhysicsWorld::gravity, Vec3(0f, -3f, 0f))
                float(IRigidBodyPhysicsWorld::fixedDelta, 0.02f)
                int(IRigidBodyPhysicsWorld::maxContacts, 40)
                bool(IRigidBodyPhysicsWorld::isSimulationRunning)

                descriptorI<IPlaneShape>(::PlaneShape) {
                    setupShape()
                    float(IPlaneShape::depth)
                    vec3(IPlaneShape::normal, MATH.Y)
                }
                descriptorI<IBoxShape>(::BoxShape) {
                    setupShape()
                    float(IBoxShape::xSize, 1f)
                    float(IBoxShape::ySize, 1f)
                    float(IBoxShape::zSize, 1f)
                }
                descriptorI<ISphereShape>(::SphereShape) {
                    setupShape()
                    float(ISphereShape::radius, 1f)
                }
                descriptorI<ICylinderShape>(::CylinderShape) {
                    setupShape()
                    float(ICylinderShape::radius, 1f)
                    float(ICylinderShape::length, 1f)
                }
                descriptorI<ICapsuleShape>(::CapsuleShape) {
                    setupShape()
                    float(ICapsuleShape::radius, 1f)
                    float(ICapsuleShape::length, 1f)
                }
                descriptorI<ITrimeshShape>(::TrimeshShape) {
                    setupShape()
                    refAbs(ITrimeshShape::mesh)
                }
                descriptorI<IRayShape>(::RayShape) {
                    setupShape()
                    vec3(IRayShape::position)
                    vec3(IRayShape::direction, MATH.Z)
                    float(IRayShape::length, 1f)
                    bool(IRayShape::useTransformNode, true)
                }
                descriptorI<IRigidBody>(::RigidBody) {
                    float(IRigidBody::maxAngularSpeed)
                    float(IRigidBody::friction, 1f)
                    bool(IRigidBody::isStatic, false)
                    bool(IRigidBody::isGravityEnabled, true)
                    bool(IRigidBody::isKinematic)
                    bool(IRigidBody::influenceOtherBodies, true)
                }
            }
        }
    }
}

fun DVector3C.toVec3(out: IVec3 = Vec3()): IVec3 {
    return out.set(get0().toFloat(), get1().toFloat(), get2().toFloat())
}
