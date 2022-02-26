package app.thelema.test.phys

import app.thelema.ecs.component
import app.thelema.ecs.mainEntity
import app.thelema.ecs.mainLoop
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.mesh.sphereMesh
import app.thelema.g3d.transformNode
import app.thelema.gl.*
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.phys.*
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class RayShapeTest: Test {
    override fun testMain() = mainEntity {

        orbitCameraControl()

        entity("box") {
            boxMesh { setSize(5f) }
            boxShape { setSize(5f) }
            rigidBody {
                isStatic = true
                node.setPosition(0f, 0f, 5f)
            }
        }

        entity("sphere") {
            sphereMesh { setSize(5f) }
            sphereShape { setSize(5f) }
            rigidBody {
                isStatic = true
                node.setPosition(0f, 0f, -10f)
            }
        }

        val intersection = entity("intersection") {
            sphereMesh { setSize(0.1f) }
            material {
                shader = SimpleShader3D {
                    setupOnlyColor(0x00FF00FF)
                }
            }
        }
        val intersectionNode = intersection.transformNode()
        val intersectionMesh = intersection.mesh()

        entity("ray") {
            val rayNode = transformNode()
            rigidBody {
                isStatic = true
                node.setPosition(0f, 0f, -2f)

                addListener(object : RigidBodyListener {
                    override fun contactBegin(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {
                        intersectionMesh.isVisible = true
                        contactUpdated(contact, body, other)
                    }

                    override fun contactUpdated(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {
                        intersectionNode.position = contact.position
                        intersectionNode.updateTransform()
                    }

                    override fun contactEnd(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {
                        intersectionMesh.isVisible = false
                        contactUpdated(contact, body, other)
                    }
                })
            }

            val ray = component<IRayShape>()
            ray.length = 6f

            GL.glLineWidth(2f)

            val mesh = mesh {
                worldMatrix = Mat4()
                primitiveType = GL_LINES
                addVertexBuffer {
                    addAttribute(Vertex.POSITION)
                    initVertexBuffer(2)
                }
            }

            val positions = mesh.positions()
            val target = Vec3()

            mainLoop {
                onUpdate { delta ->
                    rayNode.rotateAroundUp(delta)

                    val pos = ray.position
                    target.set(ray.direction).scl(ray.length).add(ray.position)

                    positions.prepare {
                        setVec3(pos)
                        nextVertex()
                        setVec3(target)
                    }
                }
            }

            material {
                shader = SimpleShader3D {
                    setupOnlyColor(0xFF0000FF.toInt())
                }
            }
        }

        rigidBodyPhysicsWorld {
            startSimulation()
        }
    }
}