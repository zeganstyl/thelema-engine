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


import app.thelema.ecs.mainEntity
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.cylinderMesh
import app.thelema.gl.meshInstance
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.phys.*
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

/** @author zeganstyl */
class CylinderShapeTest: Test {
    override fun testMain() = mainEntity {

        orbitCameraControl {
            targetDistance = 10f
        }

        val cylinder = cylinderMesh {
            radius = 1f
            length = 1f
            divisions = 32
            axis = Vec3(0f, 0f, 1f)
        }

        // material will be set to box mesh automatically
        material {
            shader = SimpleShader3D()
        }

        entity("dynamic") {
            meshInstance(cylinder.mesh)
            cylinderShape {
                radius = cylinder.radius
                length = cylinder.length
            }
            rigidBody {
                node.setPosition(0f, 3f, 0f)
            }
        }

        entity("static1") {
            meshInstance(cylinder.mesh)
            cylinderShape {
                radius = cylinder.radius
                length = cylinder.length
            }
            rigidBody {
                node.setPosition(1.25f, 0f, 0f)
                node.rotateAroundAxis(1f, 0f, 0f, MATH.PI_HALF)
                isStatic = true
            }
        }

        entity("static2") {
            meshInstance(cylinder.mesh)
            cylinderShape {
                radius = cylinder.radius
                length = cylinder.length
            }
            rigidBody {
                node.setPosition(-2f, 0f, 0f)
                node.rotateAroundAxis(1f, 0f, 0f, MATH.PI_HALF)
                isStatic = true
            }
        }

        rigidBodyPhysicsWorld {
            startSimulation()
        }
    }
}