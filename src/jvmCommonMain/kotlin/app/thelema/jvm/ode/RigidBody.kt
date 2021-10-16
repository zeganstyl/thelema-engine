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

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.g3d.TransformNodeListener
import app.thelema.math.*
import app.thelema.phys.*
import app.thelema.utils.iterate
import org.ode4j.math.DMatrix3
import org.ode4j.ode.*

/** @author zeganstyl */
class RigidBody: IRigidBody {
    override var friction: Float = 1f

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.component() ?: TransformNode()
        }

    private val nodeListener = object : TransformNodeListener {
        override fun worldMatrixChanged(node: ITransformNode) {
            updateStaticKinematic()
        }
    }

    override var node: ITransformNode = TransformNode().apply { addListener(nodeListener) }
        set(value) {
            if (field != value) {
                field.removeListener(nodeListener)
                field = value
                value.addListener(nodeListener)
                updateStaticKinematic()
            }
        }

    val shapes = ArrayList<SpecificShape<DGeom>>(1)

    override var userObject: Any = this

    override var influenceOtherBodies: Boolean = true

    override var categoryBits: Long = -1

    override var collideBits: Long = -1

    var body: DBody? = null
        set(value) {
            if (field != value) {
                field = value
                updateStaticKinematic()
            }
        }

    override var isGravityEnabled: Boolean = true
        set(value) {
            field = value
            body?.gravityMode = value
        }

    override var isEnabled: Boolean
        get() = body?.isEnabled ?: false
        set(value) {
            if (value) body?.enable() else body?.disable()
        }

    override val sourceObject: Any
        get() = body!!

    override var maxAngularSpeed: Float = 0f
        set(value) {
            field = value
            body?.maxAngularSpeed = value.toDouble()
        }

    override var isStatic: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                shapes.iterate { it.geom?.body = if (value) null else body }
                updateStaticKinematic()
            }
        }

    override var isKinematic: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) body?.setKinematic() else body?.setDynamic()
                updateStaticKinematic()
            }
        }

    var isNodeUpdateEnabled = true

    override var isSimulationRunning: Boolean = false
        set(value) {
            if (field != value) {
                field = value

                if (value) {
                    setupBody()
                } else {
                    body?.destroy()
                    body = null
                }
            }
        }

    private val tmpM = DMatrix3()
    private val tmpV3 = Vec3()
    private val tmpV4 = Vec4()

    override val spaces: List<String>
        get() = spacesInternal

    private val spacesInternal = ArrayList<String>(0)

    private var listeners: ArrayList<RigidBodyListener>? = null

    fun collided(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {
        listeners?.iterate { it.collisionBegin(contact, body, other) }
    }

    override fun addListener(listener: RigidBodyListener) {
        if (listeners == null) listeners = ArrayList()
        listeners!!.add(listener)
    }

    override fun removeListener(listener: RigidBodyListener) {
        listeners?.remove(listener)
    }

    override fun addSpace(name: String) {
        spacesInternal.add(name)
    }

    override fun removeSpace(name: String) {
        spacesInternal.remove(name)
    }

    private fun updateStaticKinematic() {
        if (isKinematic || isStatic) {
            body?.also { body ->
                val matrix = node.worldMatrix
                tmpM.set00(matrix.m00.toDouble())
                tmpM.set01(matrix.m01.toDouble())
                tmpM.set02(matrix.m02.toDouble())
                tmpM.set10(matrix.m10.toDouble())
                tmpM.set11(matrix.m11.toDouble())
                tmpM.set12(matrix.m12.toDouble())
                tmpM.set20(matrix.m20.toDouble())
                tmpM.set21(matrix.m21.toDouble())
                tmpM.set22(matrix.m22.toDouble())
                body.rotation = tmpM

                val pos = node.worldPosition
                body.setPosition(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                shapes.iterate { it.geom?.setPosition(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) }
            }
        }
    }

    private fun world(): RigidBodyPhysicsWorld? =
        entityOrNull?.getRootEntity()?.componentOrNull()

    fun setupShape(shape: SpecificShape<*>, dMass: DMass?) {
        shape.body = this
        shape.setupGeom(dMass)?.also { geom ->
            geom.collideBits = collideBits
            geom.categoryBits = categoryBits

            if (isStatic) {
                geom.setPosition(tmpV3.x.toDouble(), tmpV3.y.toDouble(), tmpV3.z.toDouble())
            } else {
                geom.body = body
            }
        }
    }

    fun setupBody() {
        world()?.also { world ->
            node.worldMatrix.getTranslation(tmpV3)
            var mass: DMass? = null

            if (!isStatic) {
                val body = OdeHelper.createBody(world.world)
                body.data = this
                body.gravityMode = isGravityEnabled
                body.setPosition(tmpV3.x.toDouble(), tmpV3.y.toDouble(), tmpV3.z.toDouble())
                mass = OdeHelper.createMass()
                this.body = body
            }

            entity.forEachComponent {
                if (it is SpecificShape<*>) {
                    setupShape(it, mass)
                }
            }
            entity.forEachChildEntity { entity ->
                entity.forEachComponent { component ->
                    if (component is SpecificShape<*>) {
                        setupShape(component, mass)
                    }
                }
            }

            body?.mass = mass
        }
    }

    override fun setLinearVelocity(x: Double, y: Double, z: Double) {
        body?.setLinearVel(x, y, z)
    }

    override fun getLinearVelocity(out: IVec3): IVec3 {
        val vec = body?.linearVel
        if (vec != null) out.set(vec.get0().toFloat(), vec.get1().toFloat(), vec.get2().toFloat())
        return out
    }

    override fun setAngularVelocity(x: Double, y: Double, z: Double) {
        body?.setAngularVel(x, y, z)
    }

    override fun getAngularVelocity(out: IVec3) {
        val vel = body?.angularVel
        if (vel != null) out.set(vel.get0().toFloat(), vel.get1().toFloat(), vel.get2().toFloat())
    }

    override fun setForce(x: Double, y: Double, z: Double) {
        body?.setForce(x, y, z)
    }

    override fun getForce(out: IVec3) {
        val force = body?.force
        if (force != null) out.set(force.get0().toFloat(), force.get1().toFloat(), force.get2().toFloat())
    }

    override fun addForce(x: Double, y: Double, z: Double) {
        body?.addForce(x, y, z)
    }

    override fun setTorque(x: Double, y: Double, z: Double) {
        body?.setTorque(x, y, z)
    }

    override fun getTorque(out: IVec3) {
        val torque = body?.torque
        if (torque != null) {
            out.set(torque.get0().toFloat(), torque.get1().toFloat(), torque.get2().toFloat())
        } else {
            out.set(0f, 0f, 0f)
        }
    }

    override fun addTorque(x: Double, y: Double, z: Double) {
        body?.addTorque(x, y, z)
    }

    override fun update() {
        val body = body
        if (isSimulationRunning && isNodeUpdateEnabled && body != null && !(isKinematic && isStatic)) {
            tmpV3.set(node.position)
            tmpV4.set(node.rotation)

            val pos = body.position
            node.position.set(pos.get0().toFloat(), pos.get1().toFloat(), pos.get2().toFloat())

            val q = body.quaternion
            node.rotation.set(q.get2().toFloat(), q.get1().toFloat(), -q.get0().toFloat(), q.get3().toFloat())

            if (node.position.isNotEqual(tmpV3) || node.rotation.isNotEqual(tmpV4)) node.requestTransformUpdate()
        }
    }
}