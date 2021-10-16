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
import app.thelema.phys.PhysicsProperties
import kotlin.math.abs

class RotateYAction: ActionAdapter() {
    override val componentName: String
        get() = "RotateYAction"

    override var actionData: ActionData = ActionData()

    /** In radians */
    var angleLength: Float = 0f

    var passed: Float = 0f

    override fun restart() {
        super.restart()
        passed = 0f
    }

    override fun update(delta: Float): Float {
        if (isRunning) {
            val node = getContextComponent<ITransformNode>()
            val physicsContext = getContextComponent<PhysicsProperties>()
            if (node != null && physicsContext != null) {
                val speed = physicsContext.angularVelocity * (if (angleLength < 0f) -delta else delta)
                val diff = angleLength - passed
                if (abs(diff) < abs(speed)) {
                    node.rotation.setQuaternionByAxis(0f, 1f, 0f, node.rotation.getQuaternionAngleAround(0f, 1f, 0f) + diff)
                    passed += diff
                    isRunning = false
                } else {
                    node.rotation.setQuaternionByAxis(0f, 1f, 0f, node.rotation.getQuaternionAngleAround(0f, 1f, 0f) + speed)
                    passed += speed
                }
                node.requestTransformUpdate()
            } else {
                isRunning = false
            }
        }
        return 0f
    }
}