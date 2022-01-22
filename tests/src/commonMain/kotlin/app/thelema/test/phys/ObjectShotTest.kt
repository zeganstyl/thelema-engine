package app.thelema.test.phys

import app.thelema.ecs.*
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.scene
import app.thelema.math.Vec3
import app.thelema.phys.*
import app.thelema.test.Test
import app.thelema.utils.LOG
import kotlin.random.Random

class ObjectShotTest: Test {
    override fun testMain() = mainEntity {
        val shapeSize = 0.1f
        val bodyMass = 1f

        val objectsNum = 10
        val shotDelay = 0.5f

        var emittedObjectsNum = 0
        var delayRemain = 0f

        orbitCameraControl {
            targetDistance = 20f
            azimuth = 2.5f
        }

        mainLoopOnUpdate {
            if (delayRemain > 0f) {
                delayRemain -= it
            } else if (delayRemain < 0f) {
                delayRemain = 0f
            }
            if (delayRemain == 0f && emittedObjectsNum < objectsNum) {
                delayRemain = shotDelay

                entity("object$emittedObjectsNum") {
                    boxMesh { setSize(1f) }
                    boxShape { setSize(shapeSize); mass = bodyMass }
                    rigidBody {
                        node.position.set(0f, Random.nextFloat(), Random.nextFloat())
                        onBodyCreated = {
                            addForce(100f, 0f, 0f)
                        }
                        startSimulation()
                    }
                }
                emittedObjectsNum++
            }
        }

        entity("static") {
            val box = boxMesh { setSize(1f, 10f, 10f) }
            boxShape { setSize(box.xSize, box.ySize, box.zSize) }
            rigidBody {
                node.position.set(100f, 0f, 0f)
                isStatic = true
                onContact { contact, body, other ->
                    LOG.info("${body.entity.name} collided with ${other.entity.name}, depth = ${contact.depth}")
                    LOG.info(">>> ${other.entity.name} velocity: ${other.getLinearVelocity(Vec3())}")
                }
            }
        }

        rigidBodyPhysicsWorld {
            gravity = Vec3(0f)
            useQuickStep = true
            fixedDelta = 0.01f
            iterations = 10
        }

        scene().startSimulation()
    }
}
