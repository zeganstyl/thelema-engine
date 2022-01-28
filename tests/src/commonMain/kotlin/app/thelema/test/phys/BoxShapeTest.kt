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

package app.thelema.test.phys

import app.thelema.app.APP
import app.thelema.ecs.Entity
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.scene
import app.thelema.gl.mesh
import app.thelema.phys.*
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class BoxShapeTest: Test {
    override val name: String
        get() = "Box shape"

    override fun testMain() {
        Entity {
            makeCurrent()

            APP.setupPhysicsComponents()

            scene()
            orbitCameraControl {
                targetDistance = 10f
            }

            val box = boxMesh {
                setSize(2f)
                mesh.isVisible = false
            }

            // material will be set to box mesh automatically
            material {
                shader = SimpleShader3D()
            }

            entity("dynamic") {
                mesh { inheritedMesh = box.mesh }
                boxShape { setSize(box.xSize, box.ySize, box.zSize) }
                rigidBody {
                    node.setPosition(0f, 3f, 0f)
                }
            }

            entity("static1") {
                mesh { inheritedMesh = box.mesh }
                boxShape { setSize(box.xSize, box.ySize, box.zSize) }
                rigidBody {
                    node.setPosition(1.25f, 0f, 0f)
                    isStatic = true
                    onContact { contact, body, other ->
                        LOG.info("${body.entity.name} collided with ${other.entity.name}, depth = ${contact.depth}, normal = ${contact.normal}")
                    }
                }
            }

            entity("static2") {
                mesh { inheritedMesh = box.mesh }
                boxShape { setSize(box.xSize, box.ySize, box.zSize) }
                rigidBody {
                    node.setPosition(-2.5f, 0f, 0f)
                    isStatic = true
                }
            }

            rigidBodyPhysicsWorld {
                startSimulation()

                addPhysicsWorldListener(object: IPhysicsWorldListener {
                    override fun contactBegin(contact: IBodyContact) {
                        LOG.info("begin contact ${contact.body1.entity.name} with ${contact.body2.entity.name}")
                    }

                    override fun contactEnd(contact: IBodyContact) {
                        LOG.info("end contact ${contact.body1.entity.name} with ${contact.body2.entity.name}")
                    }
                })
            }
        }
    }
}