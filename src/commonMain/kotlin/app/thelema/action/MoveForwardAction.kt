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

package app.thelema.action

import app.thelema.g3d.ITransformNode
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.phys.PhysicsProperties
import kotlin.math.abs

class MoveForwardAction: ActionAdapter() {
    var length: Float = 0f

    var passed: Float = 0f

    private val tmp: IVec3 = Vec3()

    override val componentName: String
        get() = "MoveToTargetAction"

    override var actionData: ActionData = ActionData(0f, null, null)

    override fun restart() {
        super.restart()
        passed = 0f
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            val node = getContextComponent<ITransformNode>()
            val physicsContext = getContextComponent<PhysicsProperties>()
            if (node != null && physicsContext != null) {
                tmp.set(0f, 0f, 1f).mul(node.rotation)

                val speed = physicsContext.linearVelocity * delta
                val diff = length - passed
                if (abs(diff) < speed) {
                    node.translate(tmp.scl(diff))
                    passed += diff
                    isRunning = false
                } else {
                    node.translate(tmp.scl(speed))
                    passed += speed
                }
                node.requestTransformUpdate(true)
            } else {
                isRunning = false
            }
        }
        return 0f
    }
}