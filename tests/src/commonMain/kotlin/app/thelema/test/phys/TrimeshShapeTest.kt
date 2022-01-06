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
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.mesh.sphereMesh
import app.thelema.g3d.scene
import app.thelema.gl.mesh
import app.thelema.phys.*
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test
import app.thelema.utils.LOG
import kotlin.random.Random

/** @author zeganstyl */
class TrimeshShapeTest: Test {
    override val name: String
        get() = "Trimesh shape"

    override fun testMain() {
        Entity {
            makeCurrent()

            APP.setupPhysicsComponents()

            scene()
            orbitCameraControl {
                targetDistance = 10f
            }

            val sphere = sphereMesh {
                setSize(1f)
                mesh.isVisible = false
            }

            entity("dynamic") {
                mesh { inheritedMesh = sphere.mesh }
                sphereShape { setSize(sphere.radius) }
                rigidBody {
                    node.position.set(0f, 5f, 0f)
                }
            }

            entity("trimesh") {
                planeMesh {
                    builder.calculateNormals = true
                    setSize(20f)
                    setDivisions(5)
                    heightProvider = { _, _ -> Random.nextFloat() * 5f }
                }
                trimeshShape() // will automatically take sibling mesh component
                rigidBody {
                    node.position.set(0f, 0f, 0f)
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