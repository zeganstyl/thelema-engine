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

package app.thelema.test.action

import app.thelema.action.ActionList
import app.thelema.action.MoveForwardAction
import app.thelema.action.RotateYAction
import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.node.TransformNode
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.phys.PhysicsContext
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class RotateMoveForwardActionTest: Test {
    override val name: String
        get() = "Rotate and move forward action"

    override fun testMain() {
        val box = BoxMesh { setSize(2f) }

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            updateCamera()
        }

        val node = Entity {
            component<PhysicsContext> {
                linearVelocity = 1f
                angularVelocity = 1f
            }
        }

        val shader = SimpleShader3D {
            worldMatrix = node.component<TransformNode>().worldMatrix
        }

        val actionList = Entity {
            component<ActionList> {
                customContext = node
                action<RotateYAction> {
                    angleLength = -3.14f * 0.5f
                }
                action<MoveForwardAction> {
                    length = 2f
                }
                action<RotateYAction> {
                    angleLength = 3.14f
                }
                action<MoveForwardAction> {
                    length = 4f
                }
                restart()
            }
        }

        APP.onUpdate = {
            ECS.update(actionList, APP.deltaTime)
            ECS.update(node, APP.deltaTime)
        }

        APP.onRender = {
            shader.render(box.mesh)
        }
    }
}